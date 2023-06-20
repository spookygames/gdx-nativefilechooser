/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2023 Spooky Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package games.spooky.gdx.nativefilechooser.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Locale;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.files.FileHandle;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserUtils;

/**
 * Implementation of a {@link NativeFileChooser} for the Android backend of a
 * libGDX application. This implementation is strongly associated with an
 * {@link AndroidApplication}, but this shouldn't be a problem for most use
 * cases.
 * 
 * <p>
 * A word of warning: the {@link NativeFileChooserConfiguration#nameFilter}
 * property of given {@link NativeFileChooserConfiguration} has no effect with
 * this implementation.
 * 
 * @see #chooseFile(NativeFileChooserConfiguration, NativeFileChooserCallback)
 * 
 * @see NativeFileChooser
 * @see NativeFileChooserConfiguration
 * @see NativeFileChooserCallback
 * 
 * @author thorthur
 * 
 */
public class AndroidFileChooser implements NativeFileChooser {

	private final static int IntentCode = 19161107;

	private final AndroidApplication app;

	/**
	 * Initialize a new {@code AndroidFileChooser} with given non-null
	 * {@link AndroidApplication}.
	 * 
	 * @param application
	 *            Application this file chooser will interact with
	 */
	public AndroidFileChooser(AndroidApplication application) {
		super();
		NativeFileChooserUtils.checkNotNull(application, "application");
		this.app = application;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see NativeFileChooser#chooseFile(NativeFileChooserConfiguration,
	 * NativeFileChooserCallback)
	 */
	@Override
	public void chooseFile(NativeFileChooserConfiguration configuration, final NativeFileChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		// Create target Intent for new Activity
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);

		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		// This one will ensure we have access to the
		// MediaStore.MediaColumns.DISPLAY_NAME property
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		// Handle MIME type filter and starting path, if any
		Uri data = null;
		String type = null;

		if (configuration.directory != null) {
			try {
				data = Uri.parse(configuration.directory.file().toURI().toURL().toString().replaceFirst("file:", "content:"));
			} catch (MalformedURLException ex) {
				app.error(getClass().getSimpleName(), "Invalid starting directory", ex);
			}
		}

		if (configuration.mimeFilter != null)
			type = normalizeMimeType(configuration.mimeFilter);

		if (data == null) {
			if (type != null) {
				intent.setType(type);
			}
		} else {
			if (type == null) {
				intent.setData(data);
			} else {
				intent.setDataAndType(data, type);
			}
		}

		// Warn if name filter was provided (not supported on this platform)
		if (configuration.nameFilter != null)
			app.debug(getClass().getSimpleName(), "nameFilter property is not supported on Android");

		// Register a listener to get a callback
		// It will deregister by itself on first call
		app.addAndroidEventListener(new AndroidEventListener() {
			@Override
			public void onActivityResult(int requestCode, int resultCode, Intent data) {

				// Don't interfere with other activity results
				if (requestCode != IntentCode)
					return;

				switch (resultCode) {
				case Activity.RESULT_CANCELED:
					// Action got cancelled
					callback.onCancellation();
					break;
				case Activity.RESULT_OK:
					try {
						FileHandle file;

						// Get the Uri of the selected file
						Uri uri = data.getData();

						// Try to build file from it
						file = fileHandleFromUri(uri);

						// Call success callback
						callback.onFileChosen(file);
					} catch (IOException ex) {
						callback.onError(ex);
					}
					break;
				default:
					break;
				}

				// Self deregistration
				app.removeAndroidEventListener(this);
			}
		});

		try {
			app.startActivityForResult(Intent.createChooser(intent, configuration.title), IntentCode);
		} catch (ActivityNotFoundException ex) {
			callback.onError(ex);
		}

	}

	private FileHandle fileHandleFromUri(Uri uri) throws IOException {
		File f = new File(uri.toString());
		if (!f.exists()) {

			// Copy stream to temp file and return that file
			File outputDir = app.getCacheDir();
			f = new File(outputDir, "~" + nameFromUri(uri));

			InputStream input = null;
			OutputStream output = null;

			try {
				input = app.getContentResolver().openInputStream(uri);
				output = new FileOutputStream(f);

				copyStream(input, output);
			} finally {
				if (input != null)
					input.close();
				if (output != null)
					output.close();
			}
		}

		return new FileHandle(f);
	}

	private String nameFromUri(Uri uri) {
		String[] projection = { MediaStore.MediaColumns.DISPLAY_NAME };
		Cursor metaCursor = app.getContentResolver().query(uri, projection, null, null, null);
		if (metaCursor != null) {
			try {
				if (metaCursor.moveToFirst()) {
					return metaCursor.getString(0);
				}
			} finally {
				metaCursor.close();
			}
		}
		return uri.getLastPathSegment();
	}

	private static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[2048];
		for (int n = in.read(buffer); n >= 0; n = in.read(buffer))
			out.write(buffer, 0, n);
	}

	/**
	 * Disclaimer: Taken directly from Android sources. I'd better have outdated
	 * code than API level fumbles here.
	 * 
	 * <p>
	 * Normalize a MIME data type.
	 *
	 * <p>
	 * A normalized MIME type has white-space trimmed, content-type parameters
	 * removed, and is lower-case. This aligns the type with Android best
	 * practices for intent filtering.
	 *
	 * <p>
	 * For example, "text/plain; charset=utf-8" becomes "text/plain".
	 * "text/x-vCard" becomes "text/x-vcard".
	 *
	 * <p>
	 * All MIME types received from outside Android (such as user input, or
	 * external sources like Bluetooth, NFC, or the Internet) should be
	 * normalized before they are used to create an Intent.
	 *
	 * @param type
	 *            MIME data type to normalize
	 * @return normalized MIME data type, or null if the input was null
	 * @see Intent#setType
	 * @see Intent#setTypeAndNormalize
	 */
	private static String normalizeMimeType(String type) {
		if (type == null) {
			return null;
		}

		type = type.trim().toLowerCase(Locale.ROOT);

		final int semicolonIndex = type.indexOf(';');
		if (semicolonIndex != -1) {
			type = type.substring(0, semicolonIndex);
		}
		return type;
	}

}
