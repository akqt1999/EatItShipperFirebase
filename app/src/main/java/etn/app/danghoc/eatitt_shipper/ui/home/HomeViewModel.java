package etn.app.danghoc.eatitt_shipper.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import etn.app.danghoc.eatitt_shipper.callckback.IShippingOrderCallbackListener;
import etn.app.danghoc.eatitt_shipper.common.Common;
import etn.app.danghoc.eatitt_shipper.model.ShipperUserModel;
import etn.app.danghoc.eatitt_shipper.model.ShippingOrderModel;

public class HomeViewModel extends ViewModel implements IShippingOrderCallbackListener {

    private MutableLiveData<List<ShippingOrderModel>> shippingOrderMutableData;
    private MutableLiveData<String> messageError;

    private IShippingOrderCallbackListener listener;

    public HomeViewModel() {

        shippingOrderMutableData=new MutableLiveData<>();
        messageError=new MutableLiveData<>();
        listener=this;

    }

    public MutableLiveData<List<ShippingOrderModel>> getShippingOrderMutableData(String shipperPhone) {
        loadOrderByShipper(shipperPhone);
        return shippingOrderMutableData;
    }

    private void loadOrderByShipper(String shipperPhone) {
        List<ShippingOrderModel>tempList=new ArrayList<>();
        Query orderRef= FirebaseDatabase.getInstance().getReference(Common.SHiPPER_ORDER_REF)
                .orderByChild("shipperPhone")
                .equalTo(Common.currentShipperUser.getPhone());// lay tat cac cac don dat hang co shipper giong std dang hang
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot orderSnapshot : snapshot.getChildren())
                {
                    ShippingOrderModel shippingOrderModel=orderSnapshot.getValue(ShippingOrderModel.class);
                    shippingOrderModel.setKey(orderSnapshot.getKey());
                    tempList.add(shippingOrderModel);
                }
                listener.onShippingLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                        listener.onShippingLoadFail(error.getMessage());
            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onShippingLoadSuccess(List<ShippingOrderModel> shippingOrderModelList) {
            shippingOrderMutableData.setValue(shippingOrderModelList);
    }

    @Override
    public void onShippingLoadFail(String message) {
        messageError.setValue(message);
    }
}