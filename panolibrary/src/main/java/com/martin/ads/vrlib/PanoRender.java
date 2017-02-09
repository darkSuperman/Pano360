package com.martin.ads.vrlib;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.martin.ads.vrlib.filters.advanced.DissolveBlendFilter;
import com.martin.ads.vrlib.filters.advanced.RiseFilter;
import com.martin.ads.vrlib.filters.advanced.Sphere2DPlugin;
import com.martin.ads.vrlib.filters.advanced.VignetteFilter;
import com.martin.ads.vrlib.filters.base.FilterGroup;
import com.martin.ads.vrlib.filters.base.OESFilter;
import com.martin.ads.vrlib.filters.base.OrthoFilter;
import com.martin.ads.vrlib.filters.imgproc.GrayScaleShaderFilter;
import com.martin.ads.vrlib.utils.BitmapUtils;
import com.martin.ads.vrlib.utils.StatusHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Ads on 2016/6/25.
 */
public class PanoRender
        implements GLSurfaceView.Renderer {
    public static String TAG = "PanoRender";

    private StatusHelper statusHelper;
    private PanoMediaPlayerWrapper panoMediaPlayerWrapper;
    private Sphere2DPlugin spherePlugin;
    private FilterGroup filterGroup;
    private OESFilter oesFilter;

    private int width,height;

    private boolean saveImg;

    //TODO: remove it
    //Adjust the value to play plane video.
    public static final boolean PLANE_VIDEO=false;
    private OrthoFilter orthoFilter;

    public PanoRender(StatusHelper statusHelper,PanoMediaPlayerWrapper panoMediaPlayerWrapper) {
        this.statusHelper=statusHelper;
        this.panoMediaPlayerWrapper = panoMediaPlayerWrapper;
        saveImg=false;

        filterGroup=new FilterGroup();

        oesFilter=new OESFilter(statusHelper.getContext());
        filterGroup.addFilter(oesFilter);

        //you can add filters here

//        filterGroup.addFilter(new GrayScaleShaderFilter(statusHelper.getContext()));
//        filterGroup.addFilter(new DissolveBlendFilter(statusHelper.getContext()));
//        filterGroup.addFilter(new RiseFilter(statusHelper.getContext()));

        spherePlugin=new Sphere2DPlugin(statusHelper);
        //TODO: this should be adjustable
        orthoFilter=new OrthoFilter(statusHelper.getContext(),
                OrthoFilter.ADJUSTING_MODE_FIT_TO_SCREEN);
        if(!PLANE_VIDEO){
            filterGroup.addFilter(spherePlugin);
            //filterGroup.addFilter(new VignetteFilter(statusHelper.getContext()));
        }else{
            panoMediaPlayerWrapper.setVideoSizeCallback(new PanoMediaPlayerWrapper.VideoSizeCallback() {
                @Override
                public void notifyVideoSizeChanged(int width, int height) {
                    orthoFilter.updateProjection(width,height);
                }
            });
            filterGroup.addFilter(orthoFilter);
        }

        //you can also add filters here
        //pay attention to the order of execution

        //TODO:remove to outer layer
        //if you want to play a plane video,remove
        //filterGroup.addFilter(spherePlugin);
        //and reset adjustingMode of GGOESFilter
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused,EGLConfig config) {
        filterGroup.init();
        panoMediaPlayerWrapper.setSurface(oesFilter.getGlOESTexture().getTextureId());
    }


    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        panoMediaPlayerWrapper.doTextureUpdate(oesFilter.getSTMatrix());
        filterGroup.onDrawFrame(oesFilter.getGlOESTexture().getTextureId());
        if (saveImg){
            BitmapUtils.sendImage(width,height,statusHelper.getContext());
            saveImg=false;
        }
        GLES20.glFinish();
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        this.width=width;
        this.height=height;

        GLES20.glViewport(0,0,width,height);
        filterGroup.onFilterChanged(width,height);
    }

    public void saveImg(){
        saveImg=true;
    }

    public Sphere2DPlugin getSpherePlugin() {
        return spherePlugin;
    }

    public FilterGroup getFilterGroup() {
        return filterGroup;
    }
}