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

/**
 * The {@code NativeFileChooser} interface. Put this one into your core project,
 * let it sink through via the initialization code in your platform-specific
 * projects and start rolling!
 * 
 * <p>
 * Be careful, not every parameter of a {@link NativeFileChooserConfiguration}
 * may be functional for every implementation of {@code NativeFileChooser}.
 * 
 * @see #chooseFile(NativeFileChooserConfiguration, NativeFileChooserCallback)
 * 
 * @see NativeFileChooserConfiguration
 * @see NativeFileChooserCallback
 * 
 * @author thorthur
 * 
 */
public interface NativeFileChooser {

	/**
	 * Launch a native UI in order to find a file and let you handle the result.
	 * This operation is asynchronous. All configuration is carried out through
	 * a non-null {@link NativeFileChooserConfiguration} object. Once the
	 * asynchronous operation ends (ie. a file is selected, or the operation is
	 * cancelled, or an error occurs), a proper method on the callback is
	 * called. See {@link NativeFileChooserCallback} for more.
	 * 
	 * <p>
	 * Be careful, not every parameter of a
	 * {@link NativeFileChooserConfiguration} may be functional for every
	 * implementation of {@link NativeFileChooser}.
	 * 
	 * @see NativeFileChooserConfiguration
	 * @see NativeFileChooserCallback
	 * 
	 * @param configuration
	 *            File choosing configuration, must not be null
	 * @param callback
	 *            File choosing asynchronous callback, must not be null
	 */
	void chooseFile(NativeFileChooserConfiguration configuration, NativeFileChooserCallback callback);

}
