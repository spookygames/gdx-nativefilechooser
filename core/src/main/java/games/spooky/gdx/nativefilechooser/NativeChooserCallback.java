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
package games.spooky.gdx.nativefilechooser;

import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;

/**
 * The base {@code NativeFileChooser} callback interface.
 * 
 * @author thorthur
 * 
 */
public interface NativeChooserCallback {

	/**
	 * Handle cancellation from the user.
	 */
	void onCancellation();

	/**
	 * Handle exception thrown during file choosing.
	 * <p>
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
	 * @param exception
	 *            Exception throw during file choosing
	 */
	void onError(Exception exception);

}
