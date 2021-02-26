package org.eclipse.tycho.compiler.jdt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerMessage;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.compiler.CompilerMessage.Kind;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

public class JDTCompilerExtended extends JDTCompiler {
	private static final String SEPARATOR = "----------";

	private static final char[] SEPARATOR_CHARS = new char[] { '/', '\\' };

	private static final char[] ADAPTER_PREFIX = "#ADAPTER#".toCharArray(); //$NON-NLS-1$

	private static final char[] ADAPTER_ENCODING = "ENCODING#".toCharArray(); //$NON-NLS-1$

	private static final char[] ADAPTER_ACCESS = "ACCESS#".toCharArray(); //$NON-NLS-1$


	public CompilerResult performCompile(CompilerConfiguration config) throws CompilerException {
		CustomCompilerConfiguration custom = new CustomCompilerConfiguration();

		File destinationDir = new File(config.getOutputLocation());

		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}

		String[] sourceFiles = getSourceFiles(config);

		if (sourceFiles.length == 0) {
			return new CompilerResult();
		}

		getLogger().info(
				"Compiling " + sourceFiles.length + " " + "source file" + (sourceFiles.length == 1 ? "" : "s") + " to "
						+ destinationDir.getAbsolutePath());

		Map<String, String> customCompilerArguments = config.getCustomCompilerArgumentsAsMap();
		checkCompilerArgs(customCompilerArguments, custom);

		String[] args = buildCompilerArguments(config, custom, sourceFiles);

		CompilerResult messages;

		if (config.isFork()) {
			String executable = config.getExecutable();

			if (StringUtils.isEmpty(executable)) {
				executable = "javac";
			}

			messages = compileOutOfProcess(config.getWorkingDirectory(), executable, args);
		} else {
			messages = compileInProcess(args, custom);
		}

		return messages;
	}
	/**
     * Compile the java sources in a external process, calling an external executable, like javac.
     * 
     * @param workingDirectory
     *            base directory where the process will be launched
     * @param executable
     *            name of the executable to launch
     * @param args
     *            arguments for the executable launched
     * @return CompilerResult with the errors and warnings encountered.
     * @throws CompilerException
     */

    CompilerResult compileOutOfProcess(File workingDirectory, String executable, String[] args)
            throws CompilerException {
        

        Commandline cli = new Commandline();

        cli.setWorkingDirectory(workingDirectory.getAbsolutePath());

        cli.setExecutable(executable);

        cli.addArguments(args);

        CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        int returnCode;

        List<CompilerMessage> messages;

        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, err);

            messages = parseModernStream(new BufferedReader(new StringReader(err.getOutput())));
        } catch (CommandLineException e) {
            throw new CompilerException("Error while executing the external compiler.", e);
        } catch (IOException e) {
            throw new CompilerException("Error while executing the external compiler.", e);
        }

        if (returnCode != 0 && messages.isEmpty()) {
            // TODO: exception?
            messages.add(new CompilerMessage("Failure executing javac,  but could not parse the error:" + EOL
                    + err.getOutput(), Kind.ERROR));
        }

        return new CompilerResult(returnCode == 0, messages);
    }


	/**
	 * check the compiler arguments. Extract from files specified using @, lines marked with
	 * ADAPTER_PREFIX These lines specify information that needs to be interpreted by us.
	 * 
	 * @param args
	 *            compiler arguments to process
	 */
	private void checkCompilerArgs(Map<String, String> args, CustomCompilerConfiguration custom) {
		for (String arg : args.keySet()) {
			if (arg.charAt(0) == '@') {
				try {
					char[] content = Util.getFileCharContent(new File(arg.substring(1)), null);
					int offset = 0;
					int prefixLength = ADAPTER_PREFIX.length;
					while ((offset = CharOperation.indexOf(ADAPTER_PREFIX, content, true, offset)) > -1) {
						int start = offset + prefixLength;
						int end = CharOperation.indexOf('\n', content, start);
						if (end == -1)
							end = content.length;
						while (CharOperation.isWhitespace(content[end])) {
							end--;
						}

						// end is inclusive, but in the API end is exclusive
						if (CharOperation.equals(ADAPTER_ENCODING, content, start, start + ADAPTER_ENCODING.length)) {
							CharOperation.replace(content, SEPARATOR_CHARS, File.separatorChar, start, end + 1);
							// file or folder level custom encoding
							start += ADAPTER_ENCODING.length;
							int encodeStart = CharOperation.lastIndexOf('[', content, start, end);
							if (start < encodeStart && encodeStart < end) {
								boolean isFile = CharOperation.equals(SuffixConstants.SUFFIX_java, content,
										encodeStart - 5, encodeStart, false);

								String str = String.valueOf(content, start, encodeStart - start);
								String enc = String.valueOf(content, encodeStart, end - encodeStart + 1);
								if (isFile) {
									if (custom.fileEncodings == null)
										custom.fileEncodings = new HashMap<>();
									// use File to translate the string into a
									// path with the correct File.seperator
									custom.fileEncodings.put(str, enc);
								} else {
									if (custom.dirEncodings == null)
										custom.dirEncodings = new HashMap<>();
									custom.dirEncodings.put(str, enc);
								}
							}
						} else if (CharOperation.equals(ADAPTER_ACCESS, content, start, start + ADAPTER_ACCESS.length)) {
							// access rules for the classpath
							start += ADAPTER_ACCESS.length;
							int accessStart = CharOperation.indexOf('[', content, start, end);
							// CharOperation.replace(content, SEPARATOR_CHARS,
							// File.separatorChar, start, accessStart);
							if (start < accessStart && accessStart < end) {
								String path = String.valueOf(content, start, accessStart - start);
								String access = String.valueOf(content, accessStart, end - accessStart + 1);
								if (custom.accessRules == null)
									custom.accessRules = new ArrayList<>();
								custom.accessRules.add(path);
								custom.accessRules.add(access);
							}
						}
						offset = end;
					}
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

}
