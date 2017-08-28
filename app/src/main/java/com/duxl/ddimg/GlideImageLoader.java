package com.duxl.ddimg;

import android.app.Activity;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.yancy.gallerypick.inter.ImageLoader;
import com.yancy.gallerypick.widget.GalleryImageView;

/**
 * 作者：Created by duxl on 2017/08/25.
 * 公司：重庆赛博丁科技发展有限公司
 * 类描述：xxx
 */

public class GlideImageLoader implements ImageLoader {

    private final static String TAG = "GlideImageLoader";

    @Override
    public void displayImage(Activity activity, Context context, String path, GalleryImageView galleryImageView, int width, int height) {
//        Glide.with(context)
//                .load(path)
//                .placeholder(R.mipmap.gallery_pick_photo)
//                .centerCrop()
//                .into(galleryImageView);

        Glide.with(activity).load(path).into(galleryImageView);
    }

    @Override
    public void clearMemoryCache() {

    }
}