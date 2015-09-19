package com.android.hhuimie.hmusic.utils;

import com.android.hhuimie.hmusic.model.MusicInfo;
import java.util.Comparator;

public class LoveCompatatorUtil<T> implements Comparator<T> {

	@Override
	public int compare(T lhs, T rhs) {
		if (lhs instanceof MusicInfo) {
			MusicInfo info1 = (MusicInfo) lhs;
			MusicInfo info2 = (MusicInfo) rhs;
			int aReturn;
			return (aReturn = compareLovePlay(info1, info2)) != 0 ? aReturn :
					       (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
							       (aReturn = compareArtist(info1, info2)) != 0 ? aReturn : compareAlbum(info1, info2);
		}
		return 0;
	}

	private int compareLovePlay(MusicInfo info1, MusicInfo info2) {
		long love1 = info1.getPlaybacks();
		long love2 = info2.getPlaybacks();
		return love1 > love2 ? -1 : love1 < love2 ? 1 : 0;
	}


	private int compareTitle(MusicInfo info1, MusicInfo info2) {
		String title1 = info1.getTitle().toUpperCase();
		String title2 = info2.getTitle().toUpperCase();
		return title1.compareTo(title2);
	}

	private int compareArtist(MusicInfo info1, MusicInfo info2) {
		String artist1 = info1.getArtist();
		String artist2 = info2.getArtist();
		return compareUnknownArtist(artist1, artist2);
	}

	private int compareAlbum(MusicInfo info1, MusicInfo info2) {
		String album1 = info1.getAlbum();
		String album2 = info2.getAlbum();

		return compareUnknownAlbum(album1, album2);
	}

	private int compareUnknownAlbum(String lhs, String rhs) {
		if (lhs.equals("未知专辑") && !rhs.equals("未知专辑")) {
			return 1;
		}
		if (rhs.equals("未知专辑") && !lhs.equals("未知专辑")) {
			return -1;
		}
		return lhs.compareToIgnoreCase(rhs);
	}

	private int compareUnknownArtist(String lhs, String rhs) {

		if (lhs.equals("未知艺术家") && !rhs.equals("未知艺术家")) {
			return 1;
		}
		if (rhs.equals("未知艺术家") && !lhs.equals("未知艺术家")) {
			return -1;
		}

		return lhs.compareToIgnoreCase(rhs);
	}

}
