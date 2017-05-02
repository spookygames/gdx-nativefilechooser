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

import java.io.FilenameFilter;

import com.badlogic.gdx.files.FileHandle;

/**
 * The configuration class for a call to
 * {@link NativeFileChooser#chooseFile(NativeFileChooserConfiguration, NativeFileChooserCallback)}
 * .
 * 
 * <p>
 * Any instance of this is to be considered a plain-old Java object (POJO) and
 * manipulated at will, code in this very library will never modify the content
 * of such instance.
 * 
 * <p>
 * Be careful, not every parameter of a {@code NativeFileChooserConfiguration}
 * may be functional for every implementation of {@link NativeFileChooser}.
 * Consult parameters individually for more details.
 * 
 * @see NativeFileChooser#chooseFile(NativeFileChooserConfiguration, NativeFileChooserCallback)
 * 
 * @see NativeFileChooser
 * @see NativeFileChooserCallback
 * 
 * @author thorthur
 * 
 */
public class NativeFileChooserConfiguration {

	/**
	 * Starting directory for the native file chooser launched.
	 * 
	 * <p>
	 * Behavior is defined only if the {@link FileHandle} instance here is a
	 * directory. Result from any other use is thus undefined but should not do
	 * much harm anyway.
	 */
	public FileHandle directory;

	/**
	 * Title of the native UI for file choosing.
	 * 
	 * <p>
	 * If null, expect native behavior for undefined title.
	 */
	public String title;

	/**
	 * A filter on MIME data type, under the form of a MIME string, like
	 * "*&#47;*" or "application/octet-stream" or "text/plain; charset=utf-8".
	 * 
	 * <p>
	 * Warning: MIME filtering on the Desktop platform is experimental and slow
	 * at best. Use at your own risk.
	 * 
	 * <p>
	 * If a {@link #nameFilter} is also provided, final filtering will be of the
	 * AND sort. Which means that a file will be deemed valid only if it
	 * satisfies MIME filtering <i>and</i> name filtering.
	 */
	public String mimeFilter;

	/**
	 * A filter on file name (or more), just like with the regular
	 * {@link FilenameFilter} that it actually is.
	 * 
	 * <p>
	 * Caution: name filtering is not supported on the Android platform.
	 * 
	 * <p>
	 * If a {@link #mimeFilter} is also provided, final filtering will be of the
	 * AND sort. Which means that a file will be deemed valid only if it
	 * satisfies MIME filtering <i>and</i> name filtering.
	 */
	public FilenameFilter nameFilter;

}
