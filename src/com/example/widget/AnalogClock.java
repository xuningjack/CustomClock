package com.example.widget;

import java.util.TimeZone;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews.RemoteView;
import com.example.android_clock.R;

/**
 * 有时针、分针、秒针的显示时钟
 */
@RemoteView
public class AnalogClock extends View {

	private Context mContext;
	private Time mCalendar;
	private Drawable mHourHand;
	private Drawable mMinuteHand;
	private Drawable mSecondHand;
	/** 时钟面板 */
	private Drawable mDial;
	private int mDialWidth;
	private int mDialHeight;
	private boolean mAttached;
	private final Handler mHandler = new Handler();
	private float mMinutes;
	private float mHour;
	private float mSecond;
	private boolean mChanged;
	/** 定时刷新 时钟界面的Handler */
	private Handler tickHandler;

	public AnalogClock(Context context) {
		this(context, null);
	}

	public AnalogClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnalogClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mCalendar = new Time();
		Resources resource = mContext.getResources();
		mDial = resource.getDrawable(R.drawable.btn_clock);
		mHourHand = resource.getDrawable(R.drawable.clock_hand_hour);
		mMinuteHand = resource.getDrawable(R.drawable.clock_hand_minute);
		mSecondHand = resource.getDrawable(R.drawable.clock_hand_second);
		mDialWidth = mDial.getIntrinsicWidth();
		mDialHeight = mDial.getIntrinsicHeight();
		prepareRefresh();
	}

	/**
	 * 准备刷新时钟面板
	 */
	public void prepareRefresh() {
		tickHandler = new Handler();
		tickHandler.post(tickRunnable);
	}

	/**
	 * 更新时钟的线程
	 */
	private Runnable tickRunnable = new Runnable() {
		public void run() {
			onTimeChanged();
			postInvalidate();
			tickHandler.postDelayed(tickRunnable, 1000); // 1s中更改1次时间
		}
	};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (!mAttached) {
			mAttached = true;
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			getContext().registerReceiver(mIntentReceiver, filter, null,
					mHandler);
		}
		mCalendar = new Time();
		onTimeChanged();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAttached) {
			getContext().unregisterReceiver(mIntentReceiver);
			mAttached = false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 设置控件大小为表盘的大小
		setMeasuredDimension(mDialWidth, mDialHeight);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mChanged = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		boolean changed = mChanged;
		if (changed) {

			mChanged = false;
		}

		int availableWidth = getRight() - getLeft();
		System.out.println("availableWidth----" + availableWidth); // 76
		int availableHeight = getBottom() - getTop();
		System.out.println("availableWidth----" + availableHeight); // 86

		int x = availableWidth / 2;
		int y = availableHeight / 2;
		System.out.println("x----" + x); // 38
		System.out.println("y----" + y); // 43

		final Drawable dial = mDial;
		int w = dial.getIntrinsicWidth();
		System.out.println("w----" + w); // 76
		int h = dial.getIntrinsicHeight();
		System.out.println("h----" + h); // 86

		boolean scaled = false;

		// 开始画面板
		if (availableWidth < w || availableHeight < h) {

			scaled = true;
			float scale = Math.min((float) availableWidth / (float) w,
					(float) availableHeight / (float) h);
			canvas.save();
			canvas.scale(scale, scale, x, y);
		}
		if (changed) {

			dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
		}
		dial.draw(canvas);

		// 开始画时针
		canvas.save();
		canvas.rotate(mHour / 12.0f * 360.0f, x, y);
		final Drawable hourHand = mHourHand;
		if (changed) {

			w = hourHand.getIntrinsicWidth();
			h = hourHand.getIntrinsicHeight();
			hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
					+ (h / 2));
		}
		hourHand.draw(canvas);

		// 开始画分针
		canvas.restore();
		canvas.save();
		canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);
		final Drawable minuteHand = mMinuteHand;
		if (changed) {

			w = minuteHand.getIntrinsicWidth();
			h = minuteHand.getIntrinsicHeight();
			minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
					+ (h / 2));
		}
		minuteHand.draw(canvas);

		// 开始画秒针
		canvas.restore();
		canvas.save();
		canvas.rotate(mSecond / 60.0f * 360.0f, x, y);
		final Drawable secondHand = mSecondHand;
		if (changed) {

			w = secondHand.getIntrinsicWidth();
			h = secondHand.getIntrinsicHeight();
			secondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
					+ (h / 2));
		}
		secondHand.draw(canvas);
		canvas.restore();
		if (scaled) {
			canvas.restore();
		}
	}

	/**
	 * 更新时间到当前时间
	 */
	private void onTimeChanged() {
		mCalendar.setToNow();
		int hour = mCalendar.hour;
		int minute = mCalendar.minute;
		int second = mCalendar.second;

		mMinutes = minute + second / 60.0f;
		mHour = hour + mMinutes / 60.0f;
		mSecond = second;
		mChanged = true;
		updateContentDescription(mCalendar);
	}

	@SuppressWarnings("deprecation")
	private void updateContentDescription(Time time) {
		final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
		String contentDescription = DateUtils.formatDateTime(mContext,
				time.toMillis(false), flags);
		setContentDescription(contentDescription);
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				String tz = intent.getStringExtra("time-zone");
				mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
			}
			onTimeChanged();
			invalidate(); // 使UI无效
		}
	};
}