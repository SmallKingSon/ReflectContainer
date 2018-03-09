package view.smallkingson.com.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class ReflectContainer extends FrameLayout {
  private final static String TAG = "ReflectContainer";
  private static final boolean DEBUG_DRAWING_TIME = true;

  private float mReflectionHeightRatio;
  private float mReflectionScale;
  private int mSpace;
  //private static final float mReflectionHeightRatio = 1.5f;
  /* Drawing tools used to create the reflection pool effect. */
  private final Paint mReflectionPaint = new Paint();
  private final Shader mShader;
  private Matrix mMatrix = new Matrix();

  public ReflectContainer(Context context) {
    this(context, null);
  }

  public ReflectContainer(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ReflectContainer(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setWillNotDraw(false);

    initAttrs(attrs);
    mShader = new LinearGradient(0, 0, 0, 1, 0x80ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
    mReflectionPaint.setShader(mShader);
    mReflectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
  }

  private void initAttrs(AttributeSet attrs) {
    if (attrs != null) {
      TypedArray typeArray =
          getContext().obtainStyledAttributes(attrs, R.styleable.ReflectContainer);
      mReflectionHeightRatio =
          typeArray.getFloat(R.styleable.ReflectContainer_reflectionHeightRatio, 0.5f);
      mReflectionScale = typeArray.getFloat(R.styleable.ReflectContainer_reflectionScale, 1f);
      mSpace = typeArray.getInt(R.styleable.ReflectContainer_space, 10);
      typeArray.recycle();
    }
  }

  @Override protected void onMeasure(int wspec, int hspec) {
    super.onMeasure(wspec, hspec);

    if (getChildCount() > 0) {
      View child = getChildAt(0);
      int childw = child.getMeasuredWidth();
      int childh = child.getMeasuredHeight();
            /* Enlarge the child's height by 33% for the reflection. */
      setMeasuredDimension(resolveSize(childw, wspec),
          resolveSize((int) (childh * (mReflectionHeightRatio + 1)) + mSpace, hspec));
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    long now;
    if (DEBUG_DRAWING_TIME) {
      now = System.currentTimeMillis();
    }
        /* Magic magic magic... */
    if (getChildCount() > 0) {
      drawReflection(canvas);
    }
    if (DEBUG_DRAWING_TIME) {
      long elapsed = System.currentTimeMillis() - now;
      Log.d(TAG, "Drawing took " + elapsed + " ms");
    }
  }

  @SuppressLint("WrongConstant") private void drawReflection(Canvas canvas) {
    View child = getChildAt(0);
    child.setDrawingCacheEnabled(true);

    child.buildDrawingCache();
    int childWidth = child.getWidth();
    int childHeight = child.getHeight();
    int height = getHeight();
    int reflectionHeight = (int) (childHeight * mReflectionHeightRatio);
        /*  
         * Save a layer so that we can render off screen initially in order to  
         * achieve the DST_OUT xfer mode. This allows us to have a non-solid  
         * background.  
         */
    canvas.saveLayer(child.getLeft(), child.getBottom(), child.getRight(),
        child.getBottom() + getBottom(), null, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
    // Draw the flipped child.
    canvas.save();
    canvas.scale(1, -mReflectionScale);
    canvas.translate(0, -childHeight * (1 + 1 / mReflectionScale) - mSpace / mReflectionScale);
    //canvas.translate(0, childHeight);
    child.draw(canvas);
    canvas.restore();
    //        /* Carve out the reflection area's alpha channel. */
    mMatrix.setScale(1, reflectionHeight);
    mMatrix.postTranslate(0, childHeight + mSpace);
    mShader.setLocalMatrix(mMatrix);
    canvas.drawRect(0, childHeight + mSpace, childWidth, height, mReflectionPaint);
    //Apply the canvas layer.
    canvas.restore();
  }

  public void refresh() {
    Log.i(TAG, "updateRefect() ");
    postInvalidate();
  }
}  