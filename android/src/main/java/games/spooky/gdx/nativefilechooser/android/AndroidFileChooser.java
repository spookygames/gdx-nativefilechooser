/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Spooky Games
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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import games.spooky.gdx.nativefilechooser.NativeChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import games.spooky.gdx.nativefilechooser.NativeFileChooserUtils;
import games.spooky.gdx.nativefilechooser.NativeFilesChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserConfiguration;

import static android.content.Intent.normalizeMimeType;

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
	public void chooseFile(final NativeFileChooserConfiguration configuration, final NativeFileChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		try {

			Intent intent = createFileSelectionIntent(configuration);

			registerCallbackListener(callback, new IntentConsumer() {
				@Override
				public void onData(Intent data) throws IOException {
					FileHandle file;

					// Get the Uri of the selected file
					Uri uri = data.getData();

					// Try to build file from it
					file = fileHandleFromUri(uri);

					// Call success callback
					callback.onFileChosen(file);
				}
			});

			startSelection(intent, configuration);
		} catch (Exception ex) {
			callback.onError(ex);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see NativeFileChooser#chooseFiles(NativeFileChooserConfiguration,
	 * NativeFilesChooserCallback)
	 */
	@Override
	public void chooseFiles(final NativeFileChooserConfiguration configuration, final NativeFilesChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		try {

			Intent intent = createFileSelectionIntent(configuration);
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

			registerCallbackListener(callback, new IntentConsumer() {
				@Override
				public void onData(Intent data) throws IOException {
					Array<FileHandle> files = new Array<>();

					ClipData clipData = data.getClipData();
					if (clipData == null) {
						files.add(fileHandleFromUri(data.getData()));
					} else {
						for (int i = 0, n = clipData.getItemCount(); i < n; i++) {
							files.add(fileHandleFromUri(clipData.getItemAt(i).getUri()));
						}
					}

					callback.onFilesChosen(files);
				}
			});

			startSelection(intent, configuration);
		} catch (Exception ex) {
			callback.onError(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see NativeFileChooser#chooseFolder(NativeFolderChooserConfiguration,
	 * NativeFolderChooserCallback)
	 */
	@Override
	public void chooseFolder(final NativeFolderChooserConfiguration configuration, final NativeFolderChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		try {

			Intent intent = createFolderSelectionIntent(configuration);

			registerCallbackListener(callback, new IntentConsumer() {
				@Override
				public void onData(Intent data) throws IOException {
					// Get the Uri of the selected file
					Uri uri = data.getData();

					// Try to build folder from it
					FileHandle folder = folderHandleFromUri(uri);

					// Call success callback
					callback.onFolderChosen(folder);
				}
			});

			startSelection(intent, configuration);
		} catch (Exception ex) {
			callback.onError(ex);
		}

	}

	private Intent createFolderSelectionIntent(final NativeFolderChooserConfiguration configuration) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			throw new IllegalStateException("Choosing folder is not supported on Android SDK < 21");
		}

		// Create target Intent for new Activity
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

		// Handle starting path, if any
        if (configuration.directory != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
				throw new IllegalStateException("Setting initial directory while choosing folder is not supported on Android SDK < 26");
			}
			try {
				intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, configuration.directory.file().toURI().toURL().toString().replaceFirst("file:", "content:"));
			} catch (MalformedURLException ex) {
				app.error(getClass().getSimpleName(), "Invalid starting directory", ex);
				throw new IllegalArgumentException("Invalid starting directory", ex);
			}
		}

        return intent;
	}

	private Intent createFileSelectionIntent(final NativeFileChooserConfiguration configuration) {

		if (configuration.intent == NativeFileChooserIntent.SAVE) {
			app.error(getClass().getSimpleName(), "SAVE intent is not supported on Android");
			throw new IllegalArgumentException("SAVE intent is not supported on Android");
		}

		// Create target Intent for new Activity
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

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
				throw new IllegalArgumentException("Invalid starting directory", ex);
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

		return intent;
	}

	private void registerCallbackListener(final NativeChooserCallback callback, final IntentConsumer onData) {

		// Register a listener to get a callback
		// It will deregister by itself on first call
		app.addAndroidEventListener(new AndroidEventListener() {
			@Override
			public void onActivityResult(int requestCode, int resultCode, Intent data) {

				// Don't interfere with other activity results
				if (requestCode != IntentCode)
					return;

				try {

					switch (resultCode) {
						case Activity.RESULT_CANCELED:
							// Action got cancelled
							callback.onCancellation();
							break;
						case Activity.RESULT_OK:
							if (data == null) {
								callback.onCancellation();
							} else {
								try {
									onData.onData(data);
								} catch (IOException ex) {
									callback.onError(ex);
								}
							}
							break;
						default:
							break;
					}
				} finally {
					// Self deregistration
					app.removeAndroidEventListener(this);
				}
			}
		});
	}

	private void startSelection(Intent intent, NativeChooserConfiguration configuration) throws ActivityNotFoundException {
		app.startActivityForResult(Intent.createChooser(intent, configuration.title), IntentCode);
	}

	private FileHandle folderHandleFromUri(Uri uri) throws IOException {
		if (uri == null)
			throw new IOException("No uri data received from intent");
		return Gdx.files.absolute(folderUriToPath(uri));
	}

	private String folderUriToPath(Uri uri) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			throw new IllegalStateException("Choosing folder is not supported on Android SDK < 21");
		}

		if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
			if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
				final List<String> segments = uri.getPathSegments();
				final String docId = segments.get(segments.size() - 1);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					if (split.length > 1) {
						return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
					} else {
						return Environment.getExternalStorageDirectory() + "/";
					}
				} else {
					return "storage/" + docId.replace(":", "/");
				}

			}
        }
        return uri.getPath();
    }

	private FileHandle fileHandleFromUri(Uri uri) throws IOException {
		if (uri == null)
			throw new IOException("No uri data received from intent");

		File f = new File(uri.toString());
		if (!f.exists()) {

			// Copy stream to temp file and return that file
			File outputDir = app.getCacheDir();
			f = new File(outputDir, "~" + nameFromUri(uri));

			InputStream input = null;
			OutputStream output = null;

			try {
				input = app.getContentResolver().openInputStream(uri);
				if (input == null)
					throw new IOException("Unable to open input stream");
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

	private interface IntentConsumer {
		void onData(Intent data) throws IOException;
	}

}
