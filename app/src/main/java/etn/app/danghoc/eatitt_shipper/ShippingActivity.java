package etn.app.danghoc.eatitt_shipper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import etn.app.danghoc.eatitt_shipper.common.Common;

import etn.app.danghoc.eatitt_shipper.model.ShippingOrderModel;
import etn.app.danghoc.eatitt_shipper.remote.IGoogleAPI;
import etn.app.danghoc.eatitt_shipper.remote.RetrofitClient;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ShippingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Marker shipperMarker;
    private ShippingOrderModel shippingOrderModel;

    //Animation
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat, lng;
    private Polyline blackPolyline, greyPolyline,redPolyline,testPolyline;
    private PolylineOptions polylineOptions, blackPolylineOption,redPolylineOptions,testPolylineOptions;
    private List<LatLng> polylineList;
    private IGoogleAPI iGoogleAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    List<LatLng>listPl=new ArrayList<>();

    @BindView(R.id.txt_order_number)
    TextView txt_order_number;
    @BindView(R.id.txt_address)
    TextView txt_address;
    @BindView(R.id.txt_date)
    TextView txt_date;
    @BindView(R.id.txt_name)
    TextView txt_name;

    @BindView(R.id.btn_start_trip)
    MaterialButton btn_start_trip;
    @BindView(R.id.btn_call)
    MaterialButton btn_call;
    @BindView(R.id.btn_done)
    MaterialButton btn_done;

    @BindView(R.id.btn_show)
    MaterialButton btn_show;
    @BindView(R.id.expanded_layout)
    ExpandableLayout expandableLayout;

    @BindView(R.id.img_food_image)
    ImageView img_food_image;
    private Polyline yelloPolyline;

    @OnClick(R.id.btn_show)
    void onShowClick() {
        if (expandableLayout.isExpanded())
            btn_show.setText("show");
        else
            btn_show.setText("hide");
        expandableLayout.toggle();
    }

    @OnClick(R.id.btn_call)
    void onCallClick() {
        if (shippingOrderModel != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //Request permission
                Dexter.withContext(this)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(ShippingActivity.this, "You must accept this permission to call user", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            }
                        }).check();


                return;
            }

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(new StringBuilder("tel:")
                    .append(shippingOrderModel.getOrderModel().getUserPhone()).toString()));
            startActivity(intent);
        }
    }

    AutocompleteSupportFragment places_fragment;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);



    //  minh lam cai nay de lam gi minh lam cai d=nay de hieu duoc van de la cai gi minh hieu duoc thu vien ban chat la cai gi o trong video nay
    @OnClick(R.id.btn_start_trip)
    void onStrartTrip() {//onStart
        String data = Paper.book().read(Common.SHIPPING_ORDER_DATA);
        Paper.book().write(Common.TRIP_START, data);
        btn_start_trip.setEnabled(false);
        drawRoutes(data);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        shippingOrderModel=new Gson().fromJson(data,new TypeToken<ShippingOrderModel>(){}.getType());

        //update location realtime
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {

                    compositeDisposable.add(iGoogleAPI.getDirections("diving",//cai nay dung de tra ve thoi gian
                            "less_driving",
                            Common.buildLocationString(location),
                            new StringBuilder().append(shippingOrderModel.getOrderModel().getLat())
                            .append(",")
                            .append(shippingOrderModel.getOrderModel().getLng()).toString(),
                            "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8"
                            ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> {

                                //get estimate time from api
                                String estimateTime="UNKNOWN";
                                JSONObject jsonObject=new JSONObject(s);
                                JSONArray routes=jsonObject.getJSONArray("routes");
                                JSONObject object=routes.getJSONObject(0);
                                JSONArray legs=object.getJSONArray("legs");
                                JSONObject legsObject=legs.getJSONObject(0);

                                //time
                                JSONObject time=legsObject.getJSONObject("duration");
                                estimateTime=time.getString("text");

                                //tim duong di ve duong di
                                Map<String,Object>update_data=new HashMap<>();
                                update_data.put("currentLat",location.getLatitude());
                                update_data.put("currentLng",location.getLongitude());
                                update_data.put("estimateTime",estimateTime);

                                FirebaseDatabase.getInstance()
                                        .getReference(Common.SHiPPER_ORDER_REF)
                                        .child(shippingOrderModel.getKey())
                                        .updateChildren(update_data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                          //      drawRoutes(data);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ShippingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });



                            },throwable -> {
                                Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                    );



                });

    }

    private boolean isInit = false;
    private Location previousLocation = null;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setShippingOrder();
     // testVe();// test success
        mMap.getUiSettings().setZoomControlsEnabled(true);
        String data;

        if (TextUtils.isEmpty(Paper.book().read(Common.TRIP_START))) {
            //just do normal
            btn_start_trip.setEnabled(true);
            data = Paper.book().read(Common.SHIPPING_ORDER_DATA);
        } else {
            btn_start_trip.setEnabled(false);
            data = Paper.book().read(Common.TRIP_START);
        }

        drawRoutes2(data);
   //     drawRoutes(data);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);

        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);



        initPlaces();
        setupAutoComplePlaces();

        ButterKnife.bind(this);

        buidLocationRequest();
        buildLocationCallback();



        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(ShippingActivity.this::onMapReady);

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ShippingActivity.this);
                        if (ActivityCompat.checkSelfPermission(ShippingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ShippingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(ShippingActivity.this, "you must enable this location permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();


    }

    private void setupAutoComplePlaces() {
        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                     drawRoutes(place);
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(ShippingActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoutes(Place place) {


        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .title(place.getName())
                .snippet(place.getAddress())
                .position(place.getLatLng())
        );

        //  add box

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    String to=new StringBuilder()
                            .append(place.getLatLng().latitude)
                            .append(",")
                            .append(place.getLatLng().longitude)
                            .toString();
                    String from=new StringBuilder()
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude())
                            .toString();

                    compositeDisposable.add(iGoogleAPI.getDirections("driving",
                            "less_driving",
                            from,to,
                            "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                           @Override
                                           public void accept(String s) throws Exception {

                                               try {
                                                   JSONObject jsonObject=new JSONObject(s);
                                                   JSONArray jsonArray=jsonObject.getJSONArray("routes");
                                                   for(int i=0;i<jsonArray.length();i++)
                                                   {
                                                       JSONObject route=jsonArray.getJSONObject(i);
                                                       JSONObject poly  =route.getJSONObject("overview_polyline");
                                                       String polyline=poly.getString("points");
                                                       polylineList=Common.decodePolyString(polyline);
                                                   }
                                                   redPolylineOptions=new PolylineOptions();
                                                   redPolylineOptions.color(Color.YELLOW);
                                                   redPolylineOptions.width(12);
                                                   redPolylineOptions.startCap(new SquareCap());
                                                   redPolylineOptions.jointType(JointType.ROUND);
                                                   redPolylineOptions.addAll(polylineList);
                                                   redPolyline=mMap.addPolyline(redPolylineOptions);

                                               }catch (Exception e)
                                               {
                                                   Toast.makeText(ShippingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                               };
                                           }
                                       }
                                    , new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(ShippingActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            )
                    );
                });

    }

    private void initPlaces() {
        Places.initialize(this, "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8");
        placesClient = Places.createClient(this);
    }

    private void setShippingOrder() {
        Paper.init(this);
        String data;

        if (TextUtils.isEmpty(Paper.book().read(Common.TRIP_START))) {
            //just do normal
            btn_start_trip.setEnabled(true);
            data = Paper.book().read(Common.SHIPPING_ORDER_DATA);
        } else {
            btn_start_trip.setEnabled(false);
            data = Paper.book().read(Common.TRIP_START);
        }

        if (!TextUtils.isEmpty(data)) {


      //      drawRoutes(data);

            shippingOrderModel = new Gson()
                    .fromJson(data, new TypeToken<ShippingOrderModel>() {
                    }.getType()); // lay du lieu tu paper

            if (shippingOrderModel != null) {
                Common.setSpanStringColor("Name ",
                        shippingOrderModel.getOrderModel().getUserName(), txt_name, Color.parseColor("#333639"));

                txt_date.setText(new StringBuilder().append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                        .format(shippingOrderModel.getOrderModel().getCreateDate())));

                Common.setSpanStringColor("No: ",
                        shippingOrderModel.getOrderModel().getKey(), txt_order_number, Color.parseColor("#673ab7"));

                Common.setSpanStringColor("Address: ",
                        shippingOrderModel.getOrderModel().getShippingAddress(), txt_address, Color.parseColor("#795548"));


                Glide.with(this)
                        .load(shippingOrderModel.getOrderModel().getCartItemList().get(0).getFoodImage()).into(img_food_image);

            }
        }
    }

    private void drawRoutes2(String data) {

        Log.d("tttttsss","drawRoutes");

        ShippingOrderModel shippingOrderModel = new Gson()
                .fromJson(data, new TypeToken<ShippingOrderModel>() {
                }.getType());

        //  add box
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_box_24))
                .title(shippingOrderModel.getOrderModel().getUserName())
                .position(new LatLng(shippingOrderModel.getOrderModel().getLat(),
                        shippingOrderModel.getOrderModel().getLng()))
                .snippet(shippingOrderModel.getOrderModel().getShippingAddress())
        );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    String to=new StringBuilder()
                            .append(shippingOrderModel.getOrderModel().getLat())
                            .append(",")
                            .append(shippingOrderModel.getOrderModel().getLng())
                            .toString();
                    String from=new StringBuilder()
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude())
                            .toString();

                    compositeDisposable.add(iGoogleAPI.getDirections("driving",
                            "less_driving",
                            from, to, "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(returnResult -> {
                                try {

                                    JSONObject jsonObject = new JSONObject(returnResult);
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyline = poly.getString("points");
                                        polylineList = Common.decodePolyString(polyline);
                                    }

                                    polylineOptions = new PolylineOptions() ;
                                    polylineOptions.color(Color.RED);
                                    polylineOptions.width(12);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polylineList);
//
//
//                                    redPolyline = mMap.addPolyline(polylineOptions);

                                    //----------------------
                                    testPolylineOptions=new PolylineOptions();

                                    Log.d("cccc",polylineList.size()+"");
                                    Log.d("cccc",polylineList.get(0)+"");
                                    Log.d("cccc",polylineList.get(1)+"");

                                    //listPl=new ArrayList<>();
                                    LatLng pos1=new LatLng(37.4189, -122.0961);
                                    LatLng pos2=new LatLng(37.4124, -122.0976);



                                    listPl=polylineList;


                                    testPolylineOptions.color(Color.YELLOW);
                                    testPolylineOptions.width(12);
                                    testPolylineOptions.startCap(new SquareCap());
                                    testPolylineOptions.jointType(JointType.ROUND);
                                    testPolylineOptions.addAll(listPl);
                                    testPolyline=mMap.addPolyline(testPolylineOptions);



                                    blackPolylineOption = new PolylineOptions();
                                    blackPolylineOption.color(Color.BLACK);
                                    blackPolylineOption.width(5);
                                    blackPolylineOption.startCap(new SquareCap());
                                    blackPolylineOption.jointType(JointType.ROUND);
                                    blackPolylineOption.addAll(polylineList);
                                    blackPolyline = mMap.addPolyline(blackPolylineOption);


                                } catch (Exception e) {
                                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d("vai", "" + e.getMessage());
                                }

                            }, throwable -> {
                                if (throwable != null)
                                    Toast.makeText(ShippingActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
                });



    }

    private void drawRoutes(String data) {

        Log.d("tttttsss","drawRoutes");

        ShippingOrderModel shippingOrderModel = new Gson()
                .fromJson(data, new TypeToken<ShippingOrderModel>() {
                }.getType());

      //  add box
            mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_box_24))
                .title(shippingOrderModel.getOrderModel().getUserName())
                .position(new LatLng(shippingOrderModel.getOrderModel().getLat(),
                        shippingOrderModel.getOrderModel().getLng()))
                .snippet(shippingOrderModel.getOrderModel().getShippingAddress())
        );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    String to=new StringBuilder()
                            .append(shippingOrderModel.getOrderModel().getLat())
                            .append(",")
                            .append(shippingOrderModel.getOrderModel().getLng())
                            .toString();
                    String from=new StringBuilder()
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude())
                            .toString();

                    compositeDisposable.add(iGoogleAPI.getDirections("driving",
                            "less_driving",
                            from, to, "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(returnResult -> {
                                try {

                                    JSONObject jsonObject = new JSONObject(returnResult);
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyline = poly.getString("points");
                                        polylineList = Common.decodePolyString(polyline);
                                    }

//                                    polylineOptions = new PolylineOptions() ;
//                                    polylineOptions.color(Color.RED);
//                                    polylineOptions.width(12);
//                                    polylineOptions.startCap(new SquareCap());
//                                    polylineOptions.jointType(JointType.ROUND);
//                                    polylineOptions.addAll(polylineList);
//
//
//                                    redPolyline = mMap.addPolyline(polylineOptions);

                                    //----------------------
                                    testPolylineOptions=new PolylineOptions();

                                    Log.d("cccc",polylineList.size()+"");
                                    Log.d("cccc",polylineList.get(0)+"");
                                    Log.d("cccc",polylineList.get(1)+"");

                                    List<LatLng>listPl=new ArrayList<>();
                                    LatLng pos1=new LatLng(37.4189, -122.0961);
                                    LatLng pos2=new LatLng(37.4124, -122.0976);

                                    testPolylineOptions.color(Color.YELLOW);
                                    testPolylineOptions.width(12);
                                    testPolylineOptions.startCap(new SquareCap());
                                    testPolylineOptions.jointType(JointType.ROUND);
                                    testPolylineOptions.addAll(listPl);
                                    testPolyline=mMap.addPolyline(testPolylineOptions);



//                                    blackPolylineOption = new PolylineOptions();
//                                    blackPolylineOption.color(Color.BLACK);
//                                    blackPolylineOption.width(5);
//                                    blackPolylineOption.startCap(new SquareCap());
//                                    blackPolylineOption.jointType(JointType.ROUND);
//                                    blackPolylineOption.addAll(polylineList);
//                                    blackPolyline = mMap.addPolyline(blackPolylineOption);


                                } catch (Exception e) {
                                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d("vai", "" + e.getMessage());
                                }

                            }, throwable -> {
                                if (throwable != null)
                                    Toast.makeText(ShippingActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
                });



    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Add a marker in Sydney and move the camera
                LatLng locationShipper = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                uploadLocation(locationResult.getLastLocation());

                if (shipperMarker == null) {
                    int height, width;
                    height = width = 80;
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                            .getDrawable(ShippingActivity.this, R.drawable.shipper_new);

                    Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false); // scaled chia ti le

                    shipperMarker = mMap.addMarker(new MarkerOptions().position(locationShipper).title("You")
                            .icon(BitmapDescriptorFactory.fromBitmap(resized))); //factory : nha che tao
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));
                    Log.d("asff", "null");
                }
                if (isInit && previousLocation != null) {

                    String from = new StringBuilder()
                            .append(previousLocation.getLatitude())
                            .append(",")
                            .append(previousLocation.getLongitude())
                            .toString();

                    String to = new StringBuilder()
                            .append(locationShipper.latitude)
                            .append(",")
                            .append(locationShipper.longitude)
                            .toString();

                    moveMarkerAnimation(shipperMarker, from, to);

                    previousLocation = locationResult.getLastLocation();
                }
                if (!isInit) {
                    isInit = true;
                    previousLocation = locationResult.getLastLocation(); // lay vi tri goc cua shipper
                }


            }
        };
    }

    private void uploadLocation(Location lastLocation) {

        String data=Paper.book().read(Common.TRIP_START);

        setShippingOrder();

        if(!TextUtils.isEmpty(data))
        {
            ShippingOrderModel shippingOrderModel=new Gson().fromJson(data,new TypeToken<ShippingOrderModel>(){}.getType());

            if(shippingOrderModel!=null) {
                compositeDisposable.add(iGoogleAPI.getDirections("diving",
                        "less_driving",
                        Common.buildLocationString(lastLocation),
                        new StringBuilder().append(shippingOrderModel.getOrderModel().getLat())
                                .append(",")
                                .append(shippingOrderModel.getOrderModel().getLng()).toString(),
                        "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8"
                        ).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(s -> {

                                    //get estimate time from api
                                    String estimateTime="UNKNOWN";
                                    JSONObject jsonObject=new JSONObject(s);
                                    JSONArray routes=jsonObject.getJSONArray("routes");
                                    JSONObject object=routes.getJSONObject(0);
                                    JSONArray legs=object.getJSONArray("legs");
                                    JSONObject legsObject=legs.getJSONObject(0);

                                    //time
                                    JSONObject time=legsObject.getJSONObject("duration");
                                    estimateTime=time.getString("text");
                                    //tim duong di ve duong di
                                    Map<String,Object>update_data=new HashMap<>();
                                    update_data.put("currentLat",lastLocation.getLatitude());
                                    update_data.put("currentLng",lastLocation.getLongitude());
                                    update_data.put("estimateTime",estimateTime);//da sua cho này rồi


                                    FirebaseDatabase.getInstance()
                                            .getReference(Common.SHiPPER_ORDER_REF)
                                            .child(shippingOrderModel.getKey())
                                            .updateChildren(update_data)
                                            .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());



                                },throwable -> {
                                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                );

            }

        }else
        {
            Toast.makeText(this, "Please press start trip", Toast.LENGTH_SHORT).show();
        }


    }

    private void moveMarkerAnimation(Marker marker, String from, String to) {
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from, to, getString("AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResult -> {
                    try {

                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = Common.decodePolyString(polyline);
                        }

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylineOption = new PolylineOptions();
                        blackPolylineOption.color(Color.BLACK);
                        blackPolylineOption.width(5);
                        blackPolylineOption.startCap(new SquareCap());
                        blackPolylineOption.jointType(JointType.ROUND);
                        blackPolylineOption.addAll(polylineList);
                        blackPolyline = mMap.addPolyline(blackPolylineOption);

                        //animator
                        ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                        polylineAnimator.setDuration(2000);
                        polylineAnimator.setInterpolator(new LinearInterpolator());
                        polylineAnimator.addUpdateListener(animation -> {
                            List<LatLng> points = greyPolyline.getPoints();
                            int percentValue = (int) animation.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int) (size * (percentValue / 100.0f));
                            List<LatLng> p = points.subList(0, newPoints);
                            blackPolyline.setPoints(p);
                        });

                        polylineAnimator.start();

                        //bike moving
                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (index < polylineList.size() - 1) {
                                    index++;
                                    next = index + 1;
                                    start = polylineList.get(index);
                                    end = polylineList.get(index);
                                }
                                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                                valueAnimator.setDuration(1500);
                                valueAnimator.setInterpolator(new LinearInterpolator());


                                valueAnimator.addUpdateListener(animation -> {
                                    v = animation.getAnimatedFraction();
                                    lng = v * end.longitude + (1 - v) * start.longitude;
                                    lat = v * end.latitude + (1 - v) * start.latitude;
                                    LatLng newPost = new LatLng(lat, lng);
                                    marker.setPosition(newPost);
                                    marker.setAnchor(0.5f, 0.5f);
                                    marker.setRotation(Common.getBearing(start, newPost));

                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));


                                });
                                valueAnimator.start();
                                if (index < polylineList.size() - 2) //reach destination (den dich)
                                    handler.postDelayed(this, 1500);
                            }


                        }, 1500);
                    } catch (Exception e) {
                        Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("vai", "" + e.getMessage());
                    }

                }, throwable -> {
                    if (throwable != null)
                        Toast.makeText(ShippingActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private String getString(String s) {
        return "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8";
    }

    private void buidLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //priority quyen uu tien
        locationRequest.setInterval(15000); // interval : khong thoi gian ; 15s
        locationRequest.setFastestInterval(10000); //
        locationRequest.setSmallestDisplacement(20f); //displacement : dich chuyen


    }






    @Override
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onDestroy();
    }
}