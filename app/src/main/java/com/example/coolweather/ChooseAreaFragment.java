package com.example.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.db.CityEntity;
import com.example.coolweather.db.CountyEntity;
import com.example.coolweather.db.ProviceEntity;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    public static final int DEFAULT_INI_CAPACITY = 10;

    public static final String PROVICE_DATA_URI = "http://guolin.tech/api/china";

    private static final String TAG = ChooseAreaFragment.class.getSimpleName();

    private ProgressDialog mProgressDialog;

    private TextView mTitleText;

    private Button mBackButton;

    private ListView mListView;

    private ArrayAdapter<String> mAdapter;

    /**
     * 保存服务器拿到所有的数据
     */
    private List<String> mDataList = new ArrayList<>(DEFAULT_INI_CAPACITY);

    private List<ProviceEntity> mProvinceList;

    private List<CityEntity> mPCityList;

    private List<CountyEntity> mCountyList;

    /**
     * 当前选中的省
     */
    private ProviceEntity mSelectedProvince;

    /**
     * 当前选中的城市
     */
    private CityEntity mSelectedCity;

    /**
     * 当前选中的级别
     */
    private int mCurrentLevel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleText = view.findViewById(R.id.title_text);
        mBackButton = view.findViewById(R.id.button);
        mListView = view.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentLevel == LEVEL_PROVINCE) {
                    mSelectedProvince = mProvinceList.get(position);
                    queryProvinces();
                }
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLevel == LEVEL_COUNTY) {

                }
            }
        });
        queryProvinces();
    }


    /**
     * 查询全国所有省，先从数据库读缓存，没有缓存服务器获取
     */
    private void queryProvinces() {
        mTitleText.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(ProviceEntity.class);
        if (mProvinceList.size() > 0) {
            mDataList.clear();
            for (ProviceEntity proviceEntity : mProvinceList) {
                mDataList.add(proviceEntity.getProviceName());
            }
            mAdapter.notifyDataSetChanged(); // 填充后通知
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(PROVICE_DATA_URI, "province");
        }
    }

    /**
     * 根据传入的地址和类型查询省市县数据
     */
    private void queryFromServer(String uri, final String type) {
        showProgressDialog();
        Log.d(TAG, "queryFromServer()+uri:"+uri);

        HttpUtil.sendOkHttpRequest(uri, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                switch (type) {
                    case "province":
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result = Utility.handleCityResponse(responseText, mSelectedProvince.getId());
                        break;
                    case "county":
                        result = Utility.handleCountyResponse(responseText, mSelectedCity.getId());
                        break;
                    default:
                        break;
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示请求服务器数据进度对话框
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载数据...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    /**
     * 关闭进度条
     */
    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
