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
package games.spooky.gdx.nativefilechooser.desktop;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.spooky.gdx.nativefilechooser.*;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * Implementation of a {@link NativeFileChooser} for the Desktop backend of a
 * libGDX application. This implementation uses AWT's {@link FileDialog}.
 * 
 * <p>
 * A word of warning: support for the
 * {@link NativeFileChooserConfiguration#mimeFilter} property of given
 * {@link NativeFileChooserConfiguration} is experimental and slow at best. Use
 * at your own risk.
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
public class DesktopFileChooser implements NativeFileChooser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see NativeFileChooser#chooseFile(NativeFileChooserConfiguration,
	 * NativeFileChooserCallback)
	 */
	@Override
	public void chooseFile(final NativeFileChooserConfiguration configuration, NativeFileChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		File[] files = openFileDialog(configuration, false);

		if (files == null || files.length == 0) {
			callback.onCancellation();
		} else {
			callback.onFileChosen(new FileHandle(files[0]));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see NativeFileChooser#chooseFiles(NativeFileChooserConfiguration,
	 * NativeFilesChooserCallback)
	 */
	@Override
	public void chooseFiles(NativeFileChooserConfiguration configuration, NativeFilesChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		File[] selectedFiles = openFileDialog(configuration, true);

		if (selectedFiles == null || selectedFiles.length == 0) {
			callback.onCancellation();
		} else {
			Array<FileHandle> result = new Array<>(selectedFiles.length);
			for (File selectedFile : selectedFiles) {
				result.add(new FileHandle(selectedFile));
			}
			callback.onFilesChosen(result);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see NativeFileChooser#chooseFolder(NativeFolderChooserConfiguration,
	 * NativeFolderChooserCallback)
	 */
	@Override
	public void chooseFolder(NativeFolderChooserConfiguration configuration, NativeFolderChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		callback.onError(new IllegalArgumentException("Choosing folder is not implemented in AWT DesktopFileChooser"));
	}

	private File[] openFileDialog(final NativeFileChooserConfiguration configuration, boolean multiple) {
		// Create awt Dialog
		FileDialog fileDialog = new FileDialog(
				(Frame) null,
				configuration.title == null ? "" : configuration.title,
				configuration.intent == NativeFileChooserIntent.SAVE ? FileDialog.SAVE : FileDialog.LOAD);

		try {
			FilenameFilter filter = createFilenameFilter(configuration);

			if (filter != null)
				fileDialog.setFilenameFilter(filter);

			// Set starting path if any
			if (configuration.directory != null)
				fileDialog.setDirectory(configuration.directory.file().getAbsolutePath());

			fileDialog.setMultipleMode(multiple);

			fileDialog.setVisible(true);

			return fileDialog.getFiles();
		} finally {
			fileDialog.dispose();
		}
	}

	static FilenameFilter createFilenameFilter(final NativeFileChooserConfiguration configuration) {
		FilenameFilter filter = null;

		// Add MIME type filter if any
		if (configuration.mimeFilter != null)
			filter = DesktopFileChooser.createMimeTypeFilter(configuration.mimeFilter);

		// Add name filter if any
		if (configuration.nameFilter != null) {
			if (filter == null) {
				filter = configuration.nameFilter;
			} else {
				// Combine filters!
				final FilenameFilter mime = filter;
				filter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return mime.accept(dir, name) && configuration.nameFilter.accept(dir, name);
					}
				};
			}
		}
		return filter;
	}

	private static FilenameFilter createMimeTypeFilter(final String mimeType) {
		return new FilenameFilter() {

			final Pattern mimePattern = NativeFileChooserUtils.mimePattern(mimeType);

			@Override
			public boolean accept(File dir, String name) {

				// Getting a Mime type is not warranted (and may be slow!)
				try {
					String mime = Files.probeContentType(new File(dir, name).toPath());

					if (mime != null) {
						// Try to get a match on Mime type
						// That's quite faulty I know!
						return mimePattern.matcher(mime).matches();
					}

				} catch (IOException ignored) {
				}

				// Accept by default, in case mime probing doesn't work
				return true;
			}
		};
	}
}
