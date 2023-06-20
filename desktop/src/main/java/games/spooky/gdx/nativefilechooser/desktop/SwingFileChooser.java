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
package games.spooky.gdx.nativefilechooser.desktop;

import com.badlogic.gdx.files.FileHandle;
import games.spooky.gdx.nativefilechooser.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Implementation of a {@link NativeFileChooser} for the Desktop backend of a
 * libGDX application. This implementation uses Swing {@link JFileChooser}.
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
public class SwingFileChooser implements NativeFileChooser {

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

		// Create Swing JFileChooser
		JFileChooser fileChooser = new JFileChooser();
		
		String title = configuration.title;
		if (title != null)
			fileChooser.setDialogTitle(title);

		FilenameFilter filter = DesktopFileChooser.createFilenameFilter(configuration);

		if (filter != null) {
			final FilenameFilter finalFilter = filter;
			fileChooser.setFileFilter(new FileFilter() {
				@Override public String getDescription() {
					return "gdx-nativefilechooser custom filter";
				}
				
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || finalFilter.accept(f.getParentFile(), f.getName());
				}
			});
			fileChooser.setAcceptAllFileFilterUsed(false);
		}

		// Set starting path if any
		if (configuration.directory != null)
			fileChooser.setCurrentDirectory(configuration.directory.file());

		// Present it to the world

		int returnState = (configuration.intent == NativeFileChooserIntent.SAVE ? fileChooser.showSaveDialog(null) : fileChooser.showOpenDialog(null));
        if (returnState == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
			FileHandle result = new FileHandle(file);
			callback.onFileChosen(result);
        } else {
			callback.onCancellation();
        }
	}
}
