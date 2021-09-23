package etn.app.danghoc.eatitt_shipper.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eatitt_shipper.R;
import etn.app.danghoc.eatitt_shipper.adapter.MyShippingOrderAdapter;
import etn.app.danghoc.eatitt_shipper.common.Common;
import etn.app.danghoc.eatitt_shipper.model.ShippingOrderModel;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    Unbinder unbinder;

    MyShippingOrderAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        homeViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
        });

        homeViewModel.getShippingOrderMutableData(Common.currentShipperUser.getPhone()).observe(this, new Observer<List<ShippingOrderModel>>() {
            @Override
            public void onChanged(List<ShippingOrderModel> shippingOrderModelList) {
                adapter=new MyShippingOrderAdapter(getContext(),shippingOrderModelList);
                recycler_order.setAdapter(adapter);
            }
        });

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(root);
        return root;
    }

    private void initViews(View root) {
        unbinder = ButterKnife.bind(this, root);

        recycler_order.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_order.setLayoutManager(layoutManager);
    }
}