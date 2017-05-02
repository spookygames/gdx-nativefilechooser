/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 Spooky Games
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
package net.spookygames.gdx.nativefilechooser;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;

/**
 * The {@code NativeFileChooser} interface. Put this one into your core project,
 * let it sink through via the initializer in your platform-specific projects
 * and start rolling!
 * 
 * Be careful, not every parameter of a {@link NativeFileChooserConfiguration}
 * may be functional for every implementation of {@code NativeFileChooser}.
 * 
 * @see NativeFileChooser#chooseFile(NativeFileChooserConfiguration, NativeFileChooserCallback)
 * 
 * @see NativeFileChooserConfiguration
 * @see NativeFileChooserCallback
 * 
 * @author thorthur
 * 
 */
public interface NativeFileChooserCallback {

	/**
	 * Handle the user-chosen {@link FileHandle}.
	 * 
	 * @param file
	 *            FileHandle chosen by user
	 */
	void onFileChosen(FileHandle file);

	/**
	 * Handle cancellation from the user.
	 * 
	 * In this case, {@link #onFileChosen(FileHandle)} will not be called.
	 */
	void onCancellation();

	/**
	 * Handle exception throw during file choosing.
	 * 
	 * On Android you should be prepared to handle:
	 * <ul>
	 * <li>
	 * {@link IOException} if an error occurred while copying chosen resource to
	 * a temporary {@link FileHandle}.
	 * </li>
	 * <li>
	 * {@code ActivityNotFoundException} if no file manager could be found on
	 * the device.
	 * </li>
	 * </ul>
	 * 
	 * In this case, {@link #onFileChosen(FileHandle)} will not be called.
	 * 
	 * @param exception
	 *            Exception throw during file choosing
	 */
	void onError(Exception exception);

}
