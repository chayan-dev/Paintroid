/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  Paintroid: An image manipulation application for Android, part of the
 *  Catroid project and Catroid suite of software.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.tugraz.ist.paintroid.command.implementation;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import at.tugraz.ist.paintroid.command.Command;
import at.tugraz.ist.paintroid.command.CommandManager;

public class CommandHandlerImplementation implements CommandManager {
	private static final int MAX_COMMANDS = 256;

	private final LinkedList<Command> mCommandQueue;
	private int mCommandCounter;
	private int mCommandIndex;
	private Bitmap mOriginalBitmap;

	public CommandHandlerImplementation(Context context) {
		mCommandQueue = new LinkedList<Command>();
		mCommandQueue.add(new ClearCommand());
		mCommandCounter = 1;
		mCommandIndex = 1;
	}

	@Override
	public void setOriginalBitmap(Bitmap bitmap) {
		mOriginalBitmap = bitmap.copy(Config.ARGB_8888, false);
		mCommandQueue.removeFirst().freeResources();
		mCommandQueue.addFirst(new BitmapCommand(mOriginalBitmap));
	}

	@Override
	public synchronized void resetAndClear() {
		if (mOriginalBitmap != null && !mOriginalBitmap.isRecycled()) {
			mOriginalBitmap.recycle();
			mOriginalBitmap = null;
		}
		for (int i = 0; i < mCommandQueue.size(); i++) {
			mCommandQueue.get(i).freeResources();
		}
		mCommandQueue.clear();
		mCommandQueue.add(new ClearCommand());
		mCommandCounter = 1;
		mCommandIndex = 1;
	}

	@Override
	public synchronized Command getNextCommand() {
		if (mCommandIndex < mCommandCounter) {
			// Log.d(PaintroidApplication.TAG, "[COMMAND] get command at index " + mCommandIndex);
			// Log.d(PaintroidApplication.TAG, "[COMMAND] command counter  " + mCommandCounter);
			return mCommandQueue.get(mCommandIndex++);
		} else {
			return null;
		}
	}

	@Override
	public synchronized boolean commitCommand(Command command) {
		// First remove any previously undone commands from the top of the queue.
		if (mCommandCounter < mCommandQueue.size()) {
			for (int i = mCommandQueue.size(); i > mCommandCounter; i--) {
				mCommandQueue.removeLast().freeResources();
			}
		}

		if (mCommandCounter == MAX_COMMANDS) {
			// TODO handle this and don't return false
			return false;
		} else {
			mCommandCounter++;
		}

		return mCommandQueue.add(command);
	}

	@Override
	public synchronized void undo() {
		if (mCommandCounter > 1) {
			mCommandCounter--;
			mCommandIndex = 0;
		}
	}

	@Override
	public synchronized void redo() {
		if (mCommandCounter < mCommandQueue.size()) {
			mCommandIndex = mCommandCounter;
			mCommandCounter++;
		}
	}
}