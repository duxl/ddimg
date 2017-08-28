package com.duxl.ddimg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.duxl.mobileframe.util.SharedPreferencesUtil;
import com.duxl.mobileframe.util.StringUtils;
import com.yancy.gallerypick.config.GalleryConfig;
import com.yancy.gallerypick.config.GalleryPick;
import com.yancy.gallerypick.inter.IHandlerCallBack;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private String TAG = "MainActivity";

    private ImageView mIvImg;

    private TextView mTvTime;
    private TextView mTvDate;
    private TextView mTvDD;
    private TextView mTvName;
    private TextView mTvLocation;

    private ImageView mIvGuide;
    private LinearLayout mLLDate;
    private LinearLayout mLLUser;

    private final String KEY_NAME = "key_name";
    private final String KEY_LOCATION = "key_location";
    private final String KEY_GUIDE = "key_guide";
    private final String KEY_PATH = "key_path";
    private SharedPreferencesUtil mSharedPreferencesUtil;

    private final int mRequestCodeForPermissionWriteData = 0;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private boolean mGuideShowed; // 已显示过向导

    private String[] mHints = {"记住了吗", "下次不用教了哟", "现在【截屏】保存图片吧"};

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 < mHints.length) {
                Toast.makeText(MainActivity.this, mHints[msg.arg1], Toast.LENGTH_SHORT).show();
                Message delayMsg = new Message();
                delayMsg.arg1 = msg.arg1 + 1;
                mHandler.sendMessageDelayed(delayMsg, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        setDateInfo(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        setTimeInfo(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        mSharedPreferencesUtil = new SharedPreferencesUtil(this);

        String saveName = mSharedPreferencesUtil.getCacheString(KEY_NAME);
        String saveLocation = mSharedPreferencesUtil.getCacheString(KEY_LOCATION);
        saveName = TextUtils.isEmpty(saveName) ? mTvName.getContentDescription().toString() : saveName;
        saveLocation = TextUtils.isEmpty(saveLocation) ? mTvLocation.getContentDescription().toString() : saveLocation;

        mTvName.setText(saveName);
        mTvLocation.setText(saveLocation);

        mGuideShowed = mSharedPreferencesUtil.getCacheBoolean(KEY_GUIDE, false);
        if(!mGuideShowed) {
            mIvGuide.setVisibility(View.VISIBLE);
            mLLDate.setVisibility(View.GONE);
            mLLUser.setVisibility(View.GONE);

        } else {
            mIvGuide.setVisibility(View.GONE);
            mLLDate.setVisibility(View.VISIBLE);
            mLLUser.setVisibility(View.VISIBLE);

            String path = mSharedPreferencesUtil.getCacheString(KEY_PATH);
            Glide.with(MainActivity.this)
                    .load(path)
                    .into(mIvImg);
        }

    }

    private void initView() {
        mIvImg = (ImageView) findViewById(R.id.ivImg_activity_main);

        mTvTime = (TextView) findViewById(R.id.tvTime_activity_main);
        mTvDate = (TextView) findViewById(R.id.tvDate_activity_main);
        mTvDD = (TextView) findViewById(R.id.tvDD_activity_main);

        mTvName = (TextView) findViewById(R.id.tvName_activity_main);
        mTvLocation = (TextView) findViewById(R.id.tvLocation_activity_main);

        AssetManager am = getAssets();
        Typeface tf = Typeface.createFromAsset(am, "fzlthjw.ttf");

        mTvTime.setTypeface(tf);
        mTvDate.setTypeface(tf);
        mTvDD.setTypeface(tf);

        mTvName.setTypeface(tf);
        mTvLocation.setTypeface(tf);

        mIvImg.setOnLongClickListener(this);
        findViewById(R.id.llTime_activity_main).setOnClickListener(this);
        mTvName.setOnClickListener(this);
        mTvLocation.setOnClickListener(this);

        mIvGuide = (ImageView) findViewById(R.id.ivGuide_activity_main);
        mLLDate = (LinearLayout) findViewById(R.id.llDate_activity_main);
        mLLUser = (LinearLayout) findViewById(R.id.llUser_activity_main);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.llTime_activity_main) { // 设置时间
            showDatePicker();

        } else if(v.getId() == R.id.tvName_activity_main) {
            final EditText editText = new EditText(this);
            editText.setText(mTvName.getText());
            editText.setSelection(editText.getText().length());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("设置姓名");
            builder.setView(editText);
            builder.setNegativeButton("取消", null);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvName.setText(editText.getText().toString().trim());
                    mSharedPreferencesUtil.cacheString(KEY_NAME, mTvName.getText().toString());
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mSharedPreferencesUtil.cacheBoolean(KEY_GUIDE,true);
                    mIvGuide.setVisibility(View.GONE);
                    if(!mGuideShowed) {
                        Message delayMsg = new Message();
                        mHandler.sendMessageDelayed(delayMsg, 1000);
                    }
                    mGuideShowed = true;
                }
            });
            alertDialog.show();
        } else if(v.getId() == R.id.tvLocation_activity_main) {
            final EditText editText = new EditText(this);
            editText.setText(mTvLocation.getText());
            editText.setSelection(editText.getText().length());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("设置位置");
            builder.setView(editText);
            builder.setNegativeButton("取消", null);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvLocation.setText(editText.getText().toString().trim());
                    mSharedPreferencesUtil.cacheString(KEY_LOCATION, mTvLocation.getText().toString());
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mSharedPreferencesUtil.cacheBoolean(KEY_GUIDE,true);
                    mIvGuide.setVisibility(View.GONE);
                    if(!mGuideShowed) {
                        Message delayMsg = new Message();
                        mHandler.sendMessageDelayed(delayMsg, 1000);
                    }
                    mGuideShowed = true;
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.ivImg_activity_main) {
            tackPic();
        }
        return true;
    }

    public void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Panel, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setTimeInfo(hourOfDay, minute);
            }
        }, mHour, mMinute, true);
        timePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "当前时间", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                setTimeInfo(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            }
        });
        timePickerDialog.show();
        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIvGuide.setImageResource(R.mipmap.guide_03);
                mLLUser.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Panel, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                setDateInfo(year, month, dayOfMonth);
                showTimePicker();
            }
        }, mYear, mMonth, mDay);
        String title = mTvDate.getText().toString();
        datePickerDialog.setTitle(StringUtils.foregroundColor(title, Color.BLUE, 0, title.length()));
        datePickerDialog.setCanceledOnTouchOutside(false);

        datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "当前日期", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                setDateInfo(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                showTimePicker();
            }
        });

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIvGuide.setImageResource(R.mipmap.guide_03);
                mLLUser.setVisibility(View.VISIBLE);
            }
        });

        try {
            //setDatePickerTitleColor(datePickerDialog.getDatePicker());
//            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
//            mAlert.setAccessible(true);
//            Object alertController = mAlert.get(datePickerDialog);
//
//            Field mTitleView = alertController.getClass().getDeclaredField("mTitleView");
//            mTitleView.setAccessible(true);
//
//            TextView titleView = (TextView) mTitleView.get(alertController);
//            titleView.setTextColor(Color.BLUE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        datePickerDialog.show();
    }

    private void setDatePickerTitleColor(ViewGroup v) {
        for(int i=0; i<v.getChildCount(); i++) {
            View childView = v.getChildAt(i);
            if(childView instanceof TextView) {
                System.out.println("XXXXXXXXXXXXXXXXX="+((TextView)childView).getText());
                ((TextView)childView).setTextColor(Color.BLUE);
            } else if(childView instanceof ViewGroup) {
                setDatePickerTitleColor((ViewGroup) childView);
            }
        }
    }

    private void setDateInfo(int year, int month, int day) {
        mYear = year;
        mMonth = month;
        mDay = day;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String weekStr = new String[]{"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"}[week - 1];
        mTvDate.setText(new SimpleDateFormat("yyyy.MM.dd").format(calendar.getTime()) + " " + weekStr);
    }

    private void setTimeInfo(int hour, int minute) {
        mHour = hour;
        mMinute = minute;

        String hourStr = hour > 9 ? String.valueOf(hour) : "0" + hour;
        String minuteStr = minute > 9 ? String.valueOf(minute) : "0" + minute;
        mTvTime.setText(hourStr + ":" + minuteStr);
    }

    private void tackPic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 拒绝过了，提示用户如果想要正常使用，要手动去设置中授权。
                    Toast.makeText(this, "请在 设置-应用管理 中开启此应用的储存授权。", Toast.LENGTH_LONG).show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, mRequestCodeForPermissionWriteData);
                }
                return;
            }
        }

        GalleryPick.getInstance().setGalleryConfig(galleryConfig).open(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == mRequestCodeForPermissionWriteData) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tackPic();
            } else {
                Toast.makeText(this, "您拒绝了权限，不能获取图片", Toast.LENGTH_LONG).show();
            }
        }
    }

    IHandlerCallBack iHandlerCallBack = new IHandlerCallBack() {
        @Override
        public void onStart() {
            Log.i(TAG, "onStart: 开启");
        }

        @Override
        public void onSuccess(List<String> photoList) {
            Log.i(TAG, "onSuccess: 返回数据");
            for (String s : photoList) {
                Log.i(TAG, s);
            }

            String path = photoList.get(0);
            mSharedPreferencesUtil.cacheString(KEY_PATH, path);
            Glide.with(MainActivity.this)
                    .load(path)
                    .into(mIvImg);

        }

        @Override
        public void onCancel() {
            Log.i(TAG, "onCancel: 取消");
        }

        @Override
        public void onFinish() {
            Log.i(TAG, "onFinish: 结束");
            mLLDate.setVisibility(View.VISIBLE);
            mIvGuide.setImageResource(R.mipmap.guide_02);
        }

        @Override
        public void onError() {
            Log.i(TAG, "onError: 出错");
        }
    };

    GalleryConfig galleryConfig = new GalleryConfig.Builder()
            .imageLoader(new GlideImageLoader())    // ImageLoader 加载框架（必填）
            .iHandlerCallBack(iHandlerCallBack)     // 监听接口（必填）
            .provider("com.duxl.ddimg.fileprovider")   // provider (必填)
            //.pathList(path)                         // 记录已选的图片
            .multiSelect(false)                      // 是否多选   默认：false
            .multiSelect(false, 9)                   // 配置是否多选的同时 配置多选数量   默认：false ， 9
            .maxSize(9)                             // 配置多选时 的多选数量。    默认：9
            .crop(false)                             // 快捷开启裁剪功能，仅当单选 或直接开启相机时有效
            .crop(false, 1, 1, 500, 500)             // 配置裁剪功能的参数，   默认裁剪比例 1:1
            .isShowCamera(true)                     // 是否现实相机按钮  默认：false
            .filePath("/Gallery/Pictures")          // 图片存放路径
            .build();

}
