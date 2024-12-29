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

import java.io.FilenameFilter;

/**
 * The base {@code NativeFileChooser} configuration class.
 * 
 * @author thorthur
 * 
 */
public abstract class NativeChooserConfiguration {

	/**
	 * Starting directory for the native file chooser.
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

}
