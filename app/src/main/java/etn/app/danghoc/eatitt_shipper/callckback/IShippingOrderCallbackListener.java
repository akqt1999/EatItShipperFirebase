package etn.app.danghoc.eatitt_shipper.callckback;

import java.util.List;

import etn.app.danghoc.eatitt_shipper.model.ShippingOrderModel;

public interface  IShippingOrderCallbackListener {
     void onShippingLoadSuccess(List<ShippingOrderModel>shippingOrderModelList);
     void onShippingLoadFail(String messageError);

}
