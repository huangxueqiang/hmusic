package com.android.hhuimie.hmusic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.MusicInfo;
import com.android.hhuimie.hmusic.slideexpandableorpinned.PinnedSectionListView;
import com.android.hhuimie.hmusic.utils.MusicInfoUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicListAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_PINNED = 1;
	public static final int CHOICE_ARTIST = 1;
	private static final int ITEM_TYPE_COUNT = 2;
	private static final int[] COLORS = new int[]{R.color.green_light, R.color.blue_light, R.color.orange_light};
	private Object o;
	private Context mContext;
	private List<MusicInfo> mMusicInfos;
	private int mPinnedCount;
	private String mSearchMsg;
	private int mSearchTitleCount;
	private int mSearchArtistCount;
	private int mChoice;
	private boolean mIsLoveList;

	public MusicListAdapter(Context context, List<MusicInfo> musicInfos, String searchMsg, int searchTitleCount, int searchArtistCount) {
		mContext = context;
		mMusicInfos = musicInfos;
		mSearchMsg = searchMsg;
		mSearchTitleCount = searchTitleCount;
		mSearchArtistCount = searchArtistCount;
	}

	public MusicListAdapter(Context context, List<MusicInfo> musicInfos, int choice) {
		mContext = context;
		mMusicInfos = musicInfos;
		mChoice = choice;
	}

	public void setIsLoveList(boolean isLoveList) {
		mIsLoveList = isLoveList;
	}

	@Override
	public int getCount() {
		return mMusicInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mMusicInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private Object getObject(int position) {
		try {
			return mMusicInfos.getClass().getMethod("get", int.class).invoke(mMusicInfos, position);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NormalViewHolder normalViewHolder;
		PinnedViewHolder pinnedViewHolder;
		int itemViewType = getItemViewType(position);
		if (TYPE_NORMAL == itemViewType) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_music, null);
				normalViewHolder = new NormalViewHolder();
				normalViewHolder.mAlbumImage = (ImageView) convertView.findViewById(R.id.album_image);
				normalViewHolder.mSongArtistOrAlbum = (TextView) convertView.findViewById(R.id.song_artist_or_album);
				normalViewHolder.mSongDuration = (TextView) convertView.findViewById(R.id.song_duration);
				normalViewHolder.mSongTitle = (TextView) convertView.findViewById(R.id.song_title);
				convertView.setTag(normalViewHolder);
			} else {
				normalViewHolder = (NormalViewHolder) convertView.getTag();
				resetNormalViewHolder(normalViewHolder);
			}
			MusicInfo musicInfo = (MusicInfo) o;
			Bitmap bitmap = MusicInfoUtil.getArtwork(mContext, musicInfo.getId(), musicInfo.getAlbumId(), false, true);
			if (bitmap != null) {
				normalViewHolder.mAlbumImage.setImageBitmap(bitmap);
			} else {
				normalViewHolder.mAlbumImage.setImageResource(R.drawable.music5);
			}
			String title = musicInfo.getTitle();
			String artist = musicInfo.getArtist();
			String album = musicInfo.getAlbum();
			String artistOrAlbum = artist;
			if (mChoice == CHOICE_ARTIST) artistOrAlbum = album;
			normalViewHolder.mSongTitle.setText(title);
			normalViewHolder.mSongArtistOrAlbum.setText(artistOrAlbum);
			if (mIsLoveList) {
				normalViewHolder.mSongDuration.setText(musicInfo.getPlaybacks() + "");
			} else
				normalViewHolder.mSongDuration.setText(MusicInfoUtil.getFormatDuration(musicInfo.getDuration()));
			if (mSearchMsg != null) {
				// 设置模式
				Pattern p = Pattern.compile(mSearchMsg.toLowerCase());
				// 声明2个SpannnableString对象，声明2个是因为其中一个要无视大小写判断，使用另一个写入
				SpannableString sp;
				SpannableString spTemp;
				// position不大于搜索标题的总数是，高亮对应的标题
				if (position <= mSearchTitleCount) {
					// 初始化
					sp = new SpannableString(title.toLowerCase());
					spTemp = new SpannableString(title);
					// 规则，标题中找到符合搜索关键字的
					Matcher m = p.matcher(sp);
					while (m.find()) {
						// 这时要使用spTemp，因为sp全部是小写，而spTemp是原辩题
						// 设置高亮，颜色为orange_light，从m.start()到m.end()，不包含前后字符
						spTemp.setSpan(new ForegroundColorSpan(parent.getResources().getColor(R.color.orange_light)), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					// 然后设置标题为已经处理高亮的spTemp
					normalViewHolder.mSongTitle.setText(spTemp);
				} else if (position <= mSearchArtistCount + mSearchTitleCount) {
					sp = new SpannableString(artist.toLowerCase());
					spTemp = new SpannableString(artist);
					Matcher m = p.matcher(sp);
					while (m.find()) {
						spTemp.setSpan(new ForegroundColorSpan(parent.getResources().getColor(R.color.green_light)), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					normalViewHolder.mSongArtistOrAlbum.setText(spTemp);
				} else {
					sp = new SpannableString(album.toLowerCase());
					spTemp = new SpannableString(album);
					Matcher m = p.matcher(sp);
					while (m.find()) {
						spTemp.setSpan(new ForegroundColorSpan(parent.getResources().getColor(R.color.blue_light)), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					normalViewHolder.mSongArtistOrAlbum.setText(spTemp);
				}
			}
		} else if (TYPE_PINNED == itemViewType) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.pinned_textview, null);
				pinnedViewHolder = new PinnedViewHolder();
				pinnedViewHolder.mPinnedTextView = (TextView) convertView.findViewById(R.id.testview);
				convertView.setTag(pinnedViewHolder);
			} else {
				pinnedViewHolder = (PinnedViewHolder) convertView.getTag();
			}
			pinnedViewHolder.mPinnedTextView.setText((String) o);
			pinnedViewHolder.mPinnedTextView.setBackgroundColor(parent.getResources().getColor(COLORS[mPinnedCount++ % COLORS.length]));
			pinnedViewHolder.mPinnedTextView.setTextSize(17);
		}
		return convertView;
	}

	private void resetNormalViewHolder(NormalViewHolder normalViewHolder) {
		normalViewHolder.mAlbumImage.setImageResource(0);
		normalViewHolder.mAlbumImage.setImageBitmap(null);
		normalViewHolder.mSongArtistOrAlbum.setText(null);
		normalViewHolder.mSongTitle.setText(null);
	}

	@Override
	public int getItemViewType(int position) {
		o = getObject(position);
		int i = TYPE_NORMAL;
		if (o != null && o instanceof String) {
			i = TYPE_PINNED;
		}
		Log.d("eqeq", "---" + o.toString() + i + "---");
		return i;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == TYPE_NORMAL;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType) {
		Log.d("qwqw", viewType + "");
		return viewType == TYPE_PINNED;
	}

	@Override
	public int getViewTypeCount() {
		return ITEM_TYPE_COUNT;
	}

	private class PinnedViewHolder {
		TextView mPinnedTextView;
	}

	private class NormalViewHolder {
		ImageView mAlbumImage;
		TextView mSongTitle;
		TextView mSongDuration;
		TextView mSongArtistOrAlbum;
	}
}
