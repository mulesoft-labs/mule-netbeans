/*
 * Copyright 2015 Facundo Lopez Kaufmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mule.tooling.netbeans.api.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.RuntimeVersion;
import org.openide.util.Exceptions;

/**
 * Utilities for the runtime.
 *
 * @author Facundo Lopez Kaufmann
 */
public class RuntimeUtils {

    /**
     * Supported OS by NetBeans
     *
     * @see
     * https://netbeans.org/community/releases/81/relnotes.html#system_requirements
     */
    public static enum OS {
        WINDOWS,
        LINUX,
        MACOS,
        OTHER;
    }
    /**
     * Constant useful to detect the proper wrapper binary.
     *
     * Represents the current OS. Is filled only with the supported OS listed by
     * NetBeans documentation {@link OS}.
     */
    public static final OS RUNNING_OS;

    /**
     * Constant useful to detect the proper wrapper binary.
     *
     * Indicates if the current OS is running with 64 bits or not.
     */
    public static final Boolean IS64;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            RUNNING_OS = OS.WINDOWS;
        } else if (osName.contains("mac os") || osName.contains("darwin")) {
            RUNNING_OS = OS.MACOS;
        } else if (osName.contains("linux")) {
            RUNNING_OS = OS.LINUX;
        } else {
            RUNNING_OS = OS.OTHER;
        }
        IS64 = System.getProperty("sun.arch.data.model", "32").equals("64");
    }

    /**
     * Returns the expected pid file name depending on the mule version used.
     *
     * @param version
     * @return if it is community .mule.pid otherwise .mule_ee.pid
     */
    public static String pidFileForVersion(RuntimeVersion version) {
        return new StringBuilder()
                .append(".")
                .append(version.getBranch().equals(MuleRuntime.BRANCH_MULE) ? "mule" : "mule_ee")
                .append(".pid")
                .toString();
    }
    
    public static boolean isWindows() {
        return RUNNING_OS.equals(OS.WINDOWS);
    }
    
    public static boolean isProcessRunning(String pid) {
        boolean found = false;
        try {
            String line;
            Process process;
            if(isWindows()) {
                process = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            } else {
                process = Runtime.getRuntime().exec("ps -e");
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = input.readLine()) != null) {
                if(!found && line.trim().startsWith(pid)) {
                    found = true;
                }
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return found;
    }
    
    public static boolean softStop(String pid) {
        if(isWindows()) {
            return false;
        }
        try {
            Process process = Runtime.getRuntime().exec("kill " + pid);
            process.waitFor();
            return true;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    public static String detectWrapperExec(File muleHome) {
        File libBootExec = new File(muleHome, "/lib/boot/exec");
        File exec;
        if (RuntimeUtils.RUNNING_OS.equals(RuntimeUtils.OS.MACOS)) {
            exec = new File(libBootExec, "wrapper-macosx-universal-32");
            if (exec.exists()) {
                return exec.getAbsolutePath();
            }
        } else if (RuntimeUtils.RUNNING_OS.equals(RuntimeUtils.OS.WINDOWS)) {
            if (RuntimeUtils.IS64) {
                exec = new File(libBootExec, "wrapper-windows-x86-64.exe");
                if (exec.exists()) {
                    return exec.getAbsolutePath();
                }
            }
            exec = new File(libBootExec, "wrapper-windows-x86-32.exe");
            if (exec.exists()) {
                return exec.getAbsolutePath();
            }
        } else if (RuntimeUtils.RUNNING_OS.equals(RuntimeUtils.OS.LINUX)) {
            if (RuntimeUtils.IS64) {
                exec = new File(libBootExec, "wrapper-linux-x86-64");
                if (exec.exists()) {
                    return exec.getAbsolutePath();
                }
            }
            exec = new File(libBootExec, "wrapper-linux-x86-32");
            if (exec.exists()) {
                return exec.getAbsolutePath();
            }
        }
        return null;
    }
}
