package com.android.hhuimie.hmusic.slideexpandableorpinned;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.BitSet;

public abstract class AbstractSlideExpandableListAdapter extends WrapperListAdapterImpl {
	private View lastOpen = null;
	private int lastOpenPosition = -1;
	private int animationDuration = 330;
	private BitSet openItems = new BitSet();
	private final SparseIntArray viewHeights = new SparseIntArray(10);
	private ViewGroup parent;

	public AbstractSlideExpandableListAdapter(ListAdapter wrapped) {
		super(wrapped);
	}

	private OnItemExpandCollapseListener expandCollapseListener;

	public interface OnItemExpandCollapseListener {
		void onExpand(View itemView, int position);

		void onCollapse(View itemView, int position);
	}

	private void notifiyExpandCollapseListener(int type, View view, int position) {
		if (expandCollapseListener != null) {
			if (type == ExpandCollapseAnimation.EXPAND) {
				expandCollapseListener.onExpand(view, position);
			} else if (type == ExpandCollapseAnimation.COLLAPSE) {
				expandCollapseListener.onCollapse(view, position);
			}
		}
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		this.parent = viewGroup;
		view = wrapped.getView(position, view, viewGroup);
		enableFor(view, position);
		return view;
	}

	public abstract View getExpandToggleButton(View parent);

	public abstract View getExpandableView(View parent);

	public int getAnimationDuration() {
		return animationDuration;
	}

	public boolean isAnyItemExpanded() {
		return (lastOpenPosition != -1) ? true : false;
	}

	public void enableFor(View parent, int position) {
		View more = getExpandToggleButton(parent);
		View itemToolbar = getExpandableView(parent);
		itemToolbar.measure(parent.getWidth(), parent.getHeight());
		enableFor(more, itemToolbar, position);
		itemToolbar.requestLayout();
	}

	private void enableFor(final View button, final View target, final int position) {
		if (target == lastOpen && position != lastOpenPosition) {
			lastOpen = null;
		}
		if (position == lastOpenPosition) {
			lastOpen = target;
		}
		int height = viewHeights.get(position, -1);
		if (height == -1) {
			viewHeights.put(position, target.getMeasuredHeight());
			updateExpandable(target, position);
		} else {
			updateExpandable(target, position);
		}
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				Animation a = target.getAnimation();
				if (a != null && a.hasStarted() && !a.hasEnded()) {
					a.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							view.performClick();
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}
					});
				} else {
					target.setAnimation(null);
					int type = target.getVisibility() == View.VISIBLE
							           ? ExpandCollapseAnimation.COLLAPSE
							           : ExpandCollapseAnimation.EXPAND;

					if (type == ExpandCollapseAnimation.EXPAND) {
						openItems.set(position, true);
					} else {
						openItems.set(position, false);
					}
					if (type == ExpandCollapseAnimation.EXPAND) {
						if (lastOpenPosition != -1 && lastOpenPosition != position) {
							if (lastOpen != null) {
								animateView(lastOpen, ExpandCollapseAnimation.COLLAPSE);
								notifiyExpandCollapseListener(
										                             ExpandCollapseAnimation.COLLAPSE,
										                             lastOpen, lastOpenPosition);
							}
							openItems.set(lastOpenPosition, false);
						}
						lastOpen = target;
						lastOpenPosition = position;
					} else if (lastOpenPosition == position) {
						lastOpenPosition = -1;
					}
					animateView(target, type);
					notifiyExpandCollapseListener(type, target, position);
				}
			}
		});
	}

	private void updateExpandable(View target, int position) {
		final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) target.getLayoutParams();
		if (openItems.get(position)) {
			target.setVisibility(View.VISIBLE);
			params.bottomMargin = 0;
		} else {
			target.setVisibility(View.GONE);
			params.bottomMargin = 0 - viewHeights.get(position);
		}
	}

	private void animateView(final View target, final int type) {
		Animation anim = new ExpandCollapseAnimation(
				                                            target,
				                                            type
		);
		anim.setDuration(getAnimationDuration());
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (type == ExpandCollapseAnimation.EXPAND) {
					if (parent instanceof ListView) {
						ListView listView = (ListView) parent;
						int movement = target.getBottom();
						Rect r = new Rect();
						boolean visible = target.getGlobalVisibleRect(r);
						Rect r2 = new Rect();
						listView.getGlobalVisibleRect(r2);
						if (!visible) {
							listView.smoothScrollBy(movement, getAnimationDuration());
						} else {
							if (r2.bottom == r.bottom) {
								listView.smoothScrollBy(movement, getAnimationDuration());
							}
						}
					}
				}
			}
		});
		target.startAnimation(anim);
	}

	public boolean collapseLastOpen() {
		if (isAnyItemExpanded()) {
			if (lastOpen != null) {
				animateView(lastOpen, ExpandCollapseAnimation.COLLAPSE);
			}
			openItems.set(lastOpenPosition, false);
			lastOpenPosition = -1;
			return true;
		}
		return false;
	}

	public Parcelable onSaveInstanceState(Parcelable parcelable) {
		SavedState ss = new SavedState(parcelable);
		ss.lastOpenPosition = this.lastOpenPosition;
		ss.openItems = this.openItems;
		return ss;
	}

	public void onRestoreInstanceState(SavedState state) {
		if (state != null) {
			this.lastOpenPosition = state.lastOpenPosition;
			this.openItems = state.openItems;
		}
	}

	private static BitSet readBitSet(Parcel src) {
		BitSet set = new BitSet();
		if (src == null) {
			return set;
		}
		int cardinality = src.readInt();

		for (int i = 0; i < cardinality; i++) {
			set.set(src.readInt());
		}
		return set;
	}

	private static void writeBitSet(Parcel dest, BitSet set) {
		int nextSetBit = -1;
		if (dest == null || set == null) {
			return;
		}
		dest.writeInt(set.cardinality());
		while ((nextSetBit = set.nextSetBit(nextSetBit + 1)) != -1) {
			dest.writeInt(nextSetBit);
		}
	}

	static class SavedState extends View.BaseSavedState {
		public BitSet openItems = null;
		public int lastOpenPosition = -1;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			lastOpenPosition = in.readInt();
			openItems = readBitSet(in);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(lastOpenPosition);
			writeBitSet(out, openItems);
		}

		public static final Creator<SavedState> CREATOR =
				new Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}
}
