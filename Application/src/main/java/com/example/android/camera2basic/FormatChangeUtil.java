package com.example.android.camera2basic;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * module  TW_APP_SnapdragonCamera
 * author  zhaoxuan
 * date  2018/12/18
 * description 将YUV_420_888转换为NV21
 */
public class FormatChangeUtil {
    static public byte[] GetNV21DataFormImageSpeed(Image image) {
        byte[] nv21Data = null;
        int height = image.getHeight();
        Log.i("yangheng", "height = " + height);
        int width = image.getWidth();
        Log.i("yangheng", "width = " + width);
        if (height % 2 != 0 || width % 2 != 0) {
            return null;
        }

        if (image.getPlanes()[0].getPixelStride() != 1) {
            return null;
        }

        if (image.getPlanes()[1].getPixelStride() != 2 || image.getPlanes()[2].getPixelStride() != 2) {
            return null;
        }
        Rect rect = image.getCropRect();
        if (rect.top != 0 || rect.left != 0) {
            return null;
        }
        int size = height * width;
        nv21Data = new byte[size * 3 / 2];

        int yRowStride = image.getPlanes()[0].getRowStride();
        int vRowStride = image.getPlanes()[2].getRowStride();

        int offset, position;
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        offset = 0;
        position = 0;
        for (int i = 0; i < height; ++i) {
            buffer.position(position);
            buffer.get(nv21Data, offset, width);
            position += yRowStride;
            offset += width;
        }
        buffer = image.getPlanes()[2].getBuffer();
        int remaining = buffer.remaining();
        position = 0;
        while (remaining >= width) {
            buffer.position(position);
            buffer.get(nv21Data, offset, width);
            position += vRowStride;
            offset += width;
            remaining -= vRowStride;
        }
        if (remaining > 0 && offset < size * 3 / 2) {
            buffer.position(position);
            buffer.get(nv21Data, offset, remaining);
        }
        return nv21Data;
    }

    /**
     * module  TW_APP_SnapdragonCamera
     * author  zhaoxuan
     * date  2019/7/2
     * description 将nv21数据转换成jpeg数据
     * date 2019/7/19
     * description 释放ByteArrayOutputStream
     */
    public static byte[] NV21toJPEG(byte[] nv21, int width, int height) {
        ByteArrayOutputStream out = null;
        byte[] result = null;
        try {
            out = new ByteArrayOutputStream();
            YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            yuv.compressToJpeg(new Rect(0, 0, width, height), 80, out);
            result = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}