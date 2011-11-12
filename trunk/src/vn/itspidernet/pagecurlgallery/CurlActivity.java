/*
   Copyright 2011 Harri Smått

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package vn.itspidernet.pagecurlgallery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

/**
 * Simple Activity for curl testing.
 *
 * @author harism
 * @modify LamPhucDuy
 */
public class CurlActivity extends Activity {

	private static final String TAG = "CurlActivity";
	private CurlView mCurlView;
	private BitmapProvider2 mBitmapProvider = null;
	private String mCurrentDir = "/mnt/sdcard";
	private String[] mCurrentItems = new String[0];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		int index = 0;
		if (getLastNonConfigurationInstance() != null) {
			index = (Integer) getLastNonConfigurationInstance();
		}
		mCurlView = (CurlView) findViewById(R.id.curl);
		mBitmapProvider = new BitmapProvider2();
		mCurlView.setBitmapProvider(mBitmapProvider);
		mCurlView.setSizeChangedObserver(new SizeChangedObserver());
		mCurlView.setCurrentIndex(index);
		mCurlView.setBackgroundColor(0xFF202830);

		// This is something somewhat experimental. Before uncommenting next
		// line, please see method comments in CurlView.
		// mCurlView.setEnableTouchPressure(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		mCurlView.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mCurlView.onResume();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mCurlView.getCurrentIndex();
	}

	public class BitmapProvider2 implements CurlView.BitmapProvider {
		Uri mCurrentUri = null;
		int mImagesCount = 0;
		Cursor mImageCur = null;

		public BitmapProvider2() {
			 Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			 String selection = Images.Media.DATA+" LIKE ?";
			 String[] selectionArgs = {mCurrentDir+"%"};
			 mImageCur = managedQuery(uri, null, selection, selectionArgs, null);
			 if (mImageCur == null) return;
			 mImageCur.moveToFirst();
			 mImagesCount = mImageCur.getCount();
		}

		public void changePath(String path) {
			if (mImageCur != null)
				mImageCur.close();
			 Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			 String selection = Images.Media.DATA+" LIKE ?";
			 String[] selectionArgs = {path+"%"};
			 mImageCur = managedQuery(uri, null, selection, selectionArgs, null);
			 if (mImageCur == null) return;
			 mImageCur.moveToFirst();
			 mImagesCount = mImageCur.getCount();
		}
		@Override
		public Bitmap getBitmap(int width, int height, int index) {
			Bitmap b = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			b.eraseColor(0xFFFFFFFF);
			Canvas c = new Canvas(b);
			mImageCur.moveToPosition(index);
			String id = mImageCur.getString(mImageCur.getColumnIndexOrThrow( Images.Media._ID));
			String data = mImageCur.getString(mImageCur.getColumnIndexOrThrow( Images.Media.DATA));
			Log.v(TAG, "_ID="+id);
			Log.v(TAG, "DATA="+data);

			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			mCurrentUri = Uri.withAppendedPath(uri, id);
			try {
				Bitmap bitmap = getBitmapFromUri(mCurrentUri);
				Drawable d = new BitmapDrawable(bitmap);
				int margin = 7;
				int border = 3;
				Rect r = new Rect(margin, margin, width - margin, height - margin);

				int imageWidth = r.width() - (border * 2);
				int imageHeight = imageWidth * d.getIntrinsicHeight()
						/ d.getIntrinsicWidth();
				if (imageHeight > r.height() - (border * 2)) {
					imageHeight = r.height() - (border * 2);
					imageWidth = imageHeight * d.getIntrinsicWidth()
							/ d.getIntrinsicHeight();
				}

				r.left += ((r.width() - imageWidth) / 2) - border;
				r.right = r.left + imageWidth + border + border;
				r.top += ((r.height() - imageHeight) / 2) - border;
				r.bottom = r.top + imageHeight + border + border;

				Paint p = new Paint();
				p.setColor(0xFFC0C0C0);
				c.drawRect(r, p);
				r.left += border;
				r.right -= border;
				r.top += border;
				r.bottom -= border;

				d.setBounds(r);
				d.draw(c);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			return b;
		}

		@Override
		public int getBitmapCount() {
			return mImagesCount;
		}

		@Override
		public Uri getCurrentUri() {
			return mCurrentUri;
		}

	}

	/**
	 * CurlView size changed observer.
	 */
	private class SizeChangedObserver implements CurlView.SizeChangedObserver {
		@Override
		public void onSizeChanged(int w, int h) {
			if (w > h) {
				mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
//				mCurlView.setMargins(.1f, .05f, .1f, .05f);
				mCurlView.setMargins(.02f, .0f, .02f, .0f);
			} else {
				mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
//				mCurlView.setMargins(.1f, .1f, .1f, .1f);
				mCurlView.setMargins(.02f, .02f, .02f, .02f);
			}
		}
	}


	public Bitmap getBitmapFromUri(Uri imageUri) throws IOException {
	    BitmapFactory.Options mOptions = new BitmapFactory.Options();
	    mOptions.inSampleSize = 3;
	    Bitmap resizeBitmap = null;

	    InputStream is = getContentResolver().openInputStream(imageUri);
	    resizeBitmap = BitmapFactory.decodeStream(is, null, mOptions);
	    is.close();

	    return resizeBitmap;
	}

	final DialogInterface.OnClickListener _listener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			String selectedItem = mCurrentItems[which];
			mCurrentDir = mCurrentDir+"/"+selectedItem;
			mCurrentItems = getDirList(mCurrentDir);
			new AlertDialog.Builder(CurlActivity.this)
				.setTitle(mCurrentDir)
				.setItems(mCurrentItems, _listener)
				.setPositiveButton("Select", _selectListener)
				.show();
		}
	};

	final DialogInterface.OnClickListener _selectListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which > -1) {
				String selectedItem = mCurrentItems[which];
				mCurrentDir = mCurrentDir+"/"+selectedItem;
			}
			mBitmapProvider.changePath(mCurrentDir);
			mCurlView.setCurrentIndex(0);
		}
	};
	private String[] getDirList(String path) {
		String[] items = null;
		try {
			final File[] _dialog_file_list = new File(path).listFiles();
				ArrayList<String> list = new ArrayList<String>();
				int count = 0;
				//ファイル名のリストを作る
				for (File file : _dialog_file_list) {
					if(file.isDirectory()){
						//ディレクトリの場合
						String name = file.getName();
						list.add(name);
						count++;
					}
				}
				items = new String[list.size()];
				for (int i = 0; i < items.length; i++) {
					items[i] = list.get(i);
				}
			}catch(SecurityException se){
				//Util.outputDebugLog(se.getMessage());
			}catch(Exception e){
					//Util.outputDebugLog(e.getMessage());
			}
			return items;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mCurrentDir = "/mnt";
		mCurrentItems = getDirList(mCurrentDir);
		new AlertDialog.Builder(this).setTitle(mCurrentDir)
			.setItems(mCurrentItems, _listener)
			.setPositiveButton("Select",_selectListener)
			.show();
		return super.onOptionsItemSelected(item);
	}

}