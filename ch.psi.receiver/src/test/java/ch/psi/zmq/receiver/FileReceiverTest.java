/**
 * 
 * Copyright 2014 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This code is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package ch.psi.zmq.receiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.psi.zmq.receiver.FileReceiver;

/**
 * 
 */
public class FileReceiverTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		FileReceiver receiver = new FileReceiver("", 1111, "");
		
		File target = new File("./target");
		PosixFileAttributes attrs = Files.readAttributes(target.toPath(), PosixFileAttributes.class);
		UserPrincipal user = attrs.owner();
		GroupPrincipal group = attrs.group();
		
		File parentFile = new File(target, "test");
		File file = new File(parentFile, "one/two/three"); // It is important to have the first . !!!
		
		Set<PosixFilePermission> permissions = new HashSet<PosixFilePermission>();
		permissions.add(PosixFilePermission.OWNER_READ);
		permissions.add(PosixFilePermission.OWNER_WRITE);
		permissions.add(PosixFilePermission.OWNER_EXECUTE);
		permissions.add(PosixFilePermission.GROUP_READ);
		permissions.add(PosixFilePermission.GROUP_WRITE);
		permissions.add(PosixFilePermission.GROUP_EXECUTE);
		
		receiver.mkdir(file, user, group, permissions);
		
		assertTrue(file.exists());
		
		attrs = Files.readAttributes(file.toPath(), PosixFileAttributes.class);
		assertEquals(user.getName(), attrs.owner().getName());
		assertEquals(group.getName(), attrs.group().getName());
		Set<PosixFilePermission> newPermissions = attrs.permissions();
		assertEquals(permissions, newPermissions);
		
		attrs = Files.readAttributes(parentFile.toPath(), PosixFileAttributes.class);
		assertEquals(user.getName(), attrs.owner().getName());
		assertEquals(group.getName(), attrs.group().getName());
		newPermissions = attrs.permissions();
		assertEquals(permissions, newPermissions);
		
		// clean filesystem
		Files.walkFileTree(parentFile.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if (exc == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
		assertFalse(file.exists());
		assertFalse(parentFile.exists());
	}

}
