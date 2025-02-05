/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import org.jboss.as.cli.batch.BatchManager;
import org.jboss.as.cli.batch.BatchedCommand;
import org.jboss.as.cli.operation.CommandLineParser;
import org.jboss.as.cli.operation.NodePathFormatter;
import org.jboss.as.cli.operation.OperationCandidatesProvider;
import org.jboss.as.cli.operation.OperationRequestAddress;
import org.jboss.as.cli.operation.ParsedCommandLine;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;


/**
 *
 * @author Alexey Loubyansky
 */
public interface CommandContext {

    /**
     * Scope for entries added to context.
     */
    public enum Scope {

        /**
         * The duration of a request.
         */
        REQUEST,
        /**
         * The duration of the context.
         */
        CONTEXT
    }

    /**
     * Returns the JBoss CLI configuration.
     * @return  CLI configuration
     */
    CliConfig getConfig();

    /**
     * Returns the current command's arguments as a string.
     * @return current command's arguments as a string or null if the command was entered w/o arguments.
     */
    String getArgumentsString();

    /**
     * Parsed command line arguments.
     * @return  parsed command line arguments.
     */
    ParsedCommandLine getParsedCommandLine();

    /**
     * Prints a string to the CLI's output. Terminates the message by writing
     * the line separator string.
     *
     * @param message the message to print
     */
    void printLine(String message);

    /**
     * Prints a string to the CLI's output. Terminates the message by writing
     * the line separator string.
     *
     * @param message the message to print
     * @param collect Whether the message to print will be collected
     */
    default void printLine(String message, boolean collect) {
        printLine(message);
    }

    /**
     * Prints a ModelNode according to the current configuration.
     *
     * @param node The ModelNode to print.
     */
    default void printDMR(ModelNode node) {
        printDMR(node, false);
    }

    /**
     * Prints a ModelNode according to the current configuration.
     *
     * @param node The ModelNode to print.
     * @param compact true for content displayed onto a single line.
     */
    default void printDMR(ModelNode node, boolean compact) {
        if (getConfig().isOutputJSON()) {
            printLine(node.toJSONString(compact));
        } else {
            if(compact) {
                printLine(Util.compactToString(node));
            } else {
                printLine(node.toString());
            }
        }
    }

    /**
     * Prints a string to the CLI's output.
     *
     * @param message the message to print
     */
    default void print(String message) {
        printLine(message);
    }

    /**
     * Prints a collection of strings as columns to the CLI's output.
     * @param col  the collection of strings to print as columns.
     */
    void printColumns(Collection<String> col);

    /**
     * Clears the screen.
     */
    void clearScreen();

    /**
     * Terminates the command line session.
     * Also closes the connection to the controller if it's still open.
     */
    void terminateSession();

    /**
     * Checks whether the session has been terminated.
     * @return
     */
    boolean isTerminated();

    /**
     * Associates an object with key. The mapping is valid until this method is called with the same key value
     * and null as the new value for this key.
     * @param scope Entry duration scope
     * @param key the key
     * @param value the value to be associated with the key
     */
    void set(Scope scope, String key, Object value);

    /**
     * Associates an object with key. The mapping is valid until this method is
     * called with the same key value and null as the new value for this key.
     *
     * @param key the key
     * @param value the value to be associated with the key
     *
     * @deprecated Use {@link #set(Scope, String, Object)} instead.
     */
    default void set(String key, Object value) {
        set(Scope.CONTEXT, key, value);
    }

    /**
     * Returns the value the key was associated with using the set(key, value) method above.
     * @param scope Entry duration scope
     * @param key the key to fetch the value for
     * @return the value associated with the key or null, if the key wasn't associated with any non-null value.
     */
    Object get(Scope scope, String key);

    /**
     * Returns the value the key was associated with using the set(key, value)
     * method above.
     *
     * @param key the key to fetch the value for
     * @return the value associated with the key or null, if the key wasn't
     * associated with any non-null value.
     *
     * @deprecated Use {@link #get(Scope, String)} instead.
     */
    default Object get(String key) {
        return get(Scope.CONTEXT, key);
    }

    /**
     * Clear the content of a scope.
     *
     * @param scope The scope to clear, can't be null.
     */
    void clear(Scope scope);

    /**
     * Removes the value the key was associated with using the set(key, value) method above.
     * If the key isn't associated with any value, the method will return null.
     * @param scope The entry duration scope
     * @param key the key to be removed
     * @return the value associated with the key or null, if the key wasn't associated with any non-null value.
     */
    Object remove(Scope scope, String key);

    /**
     * Removes the value the key was associated with using the set(key, value)
     * method above. If the key isn't associated with any value, the method will
     * return null.
     *
     * @param key the key to be removed
     * @return the value associated with the key or null, if the key wasn't
     * associated with any non-null value.
     *
     * @deprecated Use {@link #remove(Scope, String)} instead.
     */
    default Object remove(String key) {
        return remove(Scope.CONTEXT, key);
    }

    /**
     * Returns the model controller client or null if it hasn't been initialized.
     * @return the model controller client or null if it hasn't been initialized.
     */
    ModelControllerClient getModelControllerClient();

    /**
     * Connects the controller client using the default controller definition.
     *
     * The default controller will be identified as the default specified on starting the CLI will be used, if no controller was
     * specified on start up then the default defined in the CLI configuration will be used, if no default is defined then a
     * connection to remote+http://localhost:9990 will be used instead.
     *
     * @throws CommandLineException in case the attempt to connect failed
     */
    void connectController() throws CommandLineException;

    /**
     * Connects to the controller specified.
     *
     * If the controller is null then the default specified on starting the CLI will be used, if no controller was specified on
     * start up then the default defined in the CLI configuration will be used, if no default is defined then a connection to
     * remote+http://localhost:9990 will be used instead.
     *
     * @param controller the controller to connect to
     * @throws CommandLineException in case the attempt to connect failed
     */
    void connectController(String controller) throws CommandLineException;

    /**
     * Connects to the controller specified.
     *
     * If the controller is null then the default specified on starting the CLI
     * will be used, if no controller was specified on start up then the default
     * defined in the CLI configuration will be used, if no default is defined
     * then a connection to remote+http://localhost:9990 will be used instead.
     *
     * @param controller the controller to connect to
     * @param clientAddress the address the client will bind to
     * @throws CommandLineException in case the attempt to connect failed
     */
    void connectController(String controller, String clientAddress) throws CommandLineException;

    /**
     * Connects the controller client using the host and the port.
     * If the host is null, the default controller host will be used,
     * which is localhost.
     * If the port is less than zero, the default controller port will be used,
     * which is 9999.
     *
     * @deprecated Use {@link #connectController(String)} instead.
     *
     * @param host the host to connect with
     * @param port the port to connect on
     * @throws CommandLineException  in case the attempt to connect failed
     */
    @Deprecated
    void connectController(String host, int port) throws CommandLineException;

    /**
     * Bind the controller to an existing, connected client.
     */
    void bindClient(ModelControllerClient newClient);

    /**
     * Closes the previously established connection with the controller client.
     * If the connection hasn't been established, the method silently returns.
     */
    void disconnectController();

    /**
     * Returns the default host the controller client will be connected to.
     *
     * @deprecated Use {@link CommandContext#getDefaultControllerAddress()} instead.
     *
     * @return  the default host the controller client will be connected to.
     */
    @Deprecated
    String getDefaultControllerHost();

    /**
     * Returns the default port the controller client will be connected to.
     *
     * @deprecated Use {@link CommandContext#getDefaultControllerAddress()} instead.
     *
     * @return  the default port the controller client will be connected to.
     */
    @Deprecated
    int getDefaultControllerPort();

    /**
     * The default address of the default controller to connect to.
     *
     * @return The default address.
     */
    ControllerAddress getDefaultControllerAddress();

    /**
     * Returns the host the controller client is connected to or
     * null if the connection hasn't been established yet.
     *
     * @return  the host the controller client is connected to or
     * null if the connection hasn't been established yet.
     */
    String getControllerHost();

    /**
     * Returns the port the controller client is connected to.
     *
     * @return  the port the controller client is connected to.
     */
    int getControllerPort();

    /**
     * Returns the current operation request parser.
     * @return  current operation request parser.
     */
    CommandLineParser getCommandLineParser();

    /**
     * Returns the current prefix.
     * @return current prefix
     */
    OperationRequestAddress getCurrentNodePath();

    /**
     * Returns the prefix formatter.
     * @return the prefix formatter.
     */
    NodePathFormatter getNodePathFormatter();

    /**
     * Returns the provider of operation request candidates for tab-completion.
     * @return provider of operation request candidates for tab-completion.
     */
    OperationCandidatesProvider getOperationCandidatesProvider();

    /**
     * Returns the history of all the commands and operations.
     * @return  the history of all the commands and operations.
     */
    CommandHistory getHistory();

    /**
     * Checks whether the CLI is in the batch mode.
     * @return true if the CLI is in the batch mode, false - otherwise.
     */
    boolean isBatchMode();

    /**
     * Checks whether the CLI is in a workflow mode.
     *
     * @return true if the CLI is in a workflow mode, false - otherwise.
     */
    boolean isWorkflowMode();

    /**
     * Returns batch manager.
     * @return batch manager
     */
    BatchManager getBatchManager();

    /**
     * Builds an operation request from the passed in command line.
     * If the line contains a command, the command must supported the batch mode,
     * otherwise an exception will thrown.
     *
     * @param line the command line which can be an operation request or a command that can be translated into an operation request.
     * @return  the operation request
     * @throws CommandFormatException  if the operation request couldn't be built.
     */
    BatchedCommand toBatchedCommand(String line) throws CommandFormatException;

    /**
     * Builds a DMR request corresponding to the command or the operation.
     * If the line contains a command, the corresponding command handler
     * must implement org.jboss.cli.OperationCommand interface,
     * in other words the command must translate into an operation request,
     * otherwise an exception will be thrown.
     *
     * @param line  command or an operation to build a DMR request for
     * @return  DMR request corresponding to the line
     * @throws CommandFormatException  thrown in case the line couldn't be
     * translated into a DMR request
     */
    ModelNode buildRequest(String line) throws CommandFormatException;

    /**
     * Returns the default command line completer.
     * @return  the default command line completer.
     */
    CommandLineCompleter getDefaultCommandCompleter();

    /**
     * Indicates whether the CLI is in the domain mode or standalone one (assuming established
     * connection to the controller).
     * @return  true if the CLI is connected to the domain controller, otherwise false.
     */
    boolean isDomainMode();

    /**
     * Adds a listener for CLI events.
     * @param listener  the listener
     */
    void addEventListener(CliEventListener listener);

    /**
     * Returns value that should be used as the exit code of the JVM process.
     * @return  JVM exit code
     */
    int getExitCode();

    /**
     * Executes a command or an operation. Or, if the context is in the batch mode
     * and the command is allowed in the batch, adds the command (or the operation)
     * to the currently active batch.
     * NOTE: errors are not handled by this method, they won't affect the exit code or
     * even be logged. Error handling is the responsibility of the caller.
     *
     * @param line  command or operation to handle
     * @throws CommandFormatException  in case there was an error handling the command or operation
     */
    void handle(String line) throws CommandLineException;

    /**
     * Executes a command or an operation. Or, if the context is in the batch mode
     * and the command is allowed in the batch, adds the command (or the operation)
     * to the currently active batch.
     * NOTE: unlike handle(String line), this method catches CommandLineException
     * exceptions thrown by command handlers, logs them and sets the exit code
     * status to indicate that the command or the operation has failed.
     * It's up to the caller to check the exit code with getExitCode()
     * to find out whether the command or the operation succeeded or failed.
     *
     * @param line  command or operation to handle
     * @throws CommandFormatException  in case there was an error handling the command or operation
     */
    void handleSafe(String line);

    /**
     * This method will start an interactive session.
     * It requires an initialized at the construction time console.
     */
    void interact();

    /**
     * Returns current default filesystem directory.
     * @return  current default filesystem directory.
     */
    File getCurrentDir();

    /**
     * Changes the current default filesystem directory to the argument.
     * @param dir  the new default directory
     */
    void setCurrentDir(File dir);

    /**
     * Command argument or operation parameter values may contain system properties.
     * If this method returns true then the CLI will try to resolve
     * the system properties before sending the operation request to the controller.
     * Otherwise, the resolution will happen on the server side.
     *
     * @return true if system properties in the operation parameter values
     * should be resolved by the CLI before the request is sent to the controller,
     * false if system properties should be resolved on the server side.
     */
    boolean isResolveParameterValues();

    /**
     * Command argument or operation parameter values may contain system properties.
     * If this property is set to true then the CLI will try to resolve
     * the system properties before sending the operation request to the controller.
     * Otherwise, the resolution will happen on the server side.
     *
     * @param resolve  true if system properties in the operation parameter values
     * should be resolved by the CLI before the request is sent to the controller,
     * false if system properties should be resolved on the server side.
     */
    void setResolveParameterValues(boolean resolve);

    /**
     * Whether the info or error messages should be written to the terminal output.
     *
     * The output of the info and error messages is done in the following way:
     * 1) the message is always logged using a logger
     *    (which is disabled in the config by default);
     * 2) if the output target was specified on the command line using '>'
     *    it would be used;
     * 3) if the output target was not specified, whether the message is
     *    written or not to the terminal output will depend on
     *    whether it's a silent mode or not.
     *
     * @return  true if the CLI is in the silent mode, i.e. not writing info
     *          and error messages to the terminal output, otherwise - false.
     */
    boolean isSilent();

    /**
     * Enables of disables the silent mode.
     *
     * @param silent  true if the CLI should go into the silent mode,
     *                false if the CLI should resume writing info
     *                and error messages to the terminal output.
     */
    void setSilent(boolean silent);

    /**
     * Returns the current terminal window width in case the console
     * has been initialized. Otherwise -1.
     *
     * @return  current terminal with if the console has been initialized,
     *          -1 otherwise
     */
    int getTerminalWidth();

    /**
     * Returns the current terminal window height in case the console
     * has been initialized. Otherwise -1.
     *
     * @return  current terminal height if the console has been initialized,
     *          -1 otherwise
     */
    int getTerminalHeight();

    /**
     * Initializes a variable with the given name with the given value.
     * The name of the variable must follow the rules for Java identifiers
     * but not contain '$' character.
     * If the variable already exists, its value will be silently overridden.
     * Passing in null as the value will remove the variable altogether.
     * If the variable with the given name has not been defined and the value
     * passed in is null, the method will return silently.
     *
     * @param name  name of the variable
     * @param value  value for the variable
     * @throws CommandLineException  in case the name contains illegal characters
     */
    void setVariable(String name, String value) throws CommandLineException;

    /**
     * Returns the value for the variable. If the variable has not been defined
     * the method will return null.
     *
     * @param name  name of the variable
     * @return  the value of the variable or null if the variable has not been
     *          defined
     */
    String getVariable(String name);

    /**
     * Returns a collection of all the defined variable names.
     * If there no variables defined, an empty collection will be returned.
     *
     * @return  collection of all the defined variable names or
     *          an empty collection if no variables has been defined
     */
    Collection<String> getVariables();

    /**
     * After this method returns command line handling will be redirected to
     * the passed in CommandLineRedirection instance.
     *
     * @param redirection  command line redirection handler
     * @throws CommandLineException  in case registration fails (e.g. in case
     *  one has already been registered
     */
    void registerRedirection(CommandLineRedirection redirection) throws CommandLineException;

    /**
     *  The ConnectionInfo bean is set after the connection is established to the server.
     *
     *  @return information about the current connection to the server.
     */
    ConnectionInfo getConnectionInfo() ;

    /**
     * Redirect output to the given print stream.
     * @param captor stream to which output should be written. Cannot be {@code null}
     *
     * @throws java.lang.IllegalStateException if output is already being captured
     */
    void captureOutput(PrintStream captor);

    /**
     * Stops redirecting output to the stream passed to {@link #captureOutput(java.io.PrintStream)}.
     *
     * @throws java.lang.IllegalStateException if output isn't currently being captured
     */
    void releaseOutput();

    /**
     * Set the command timeout to a number of seconds.
     * Value equals to 0 means no timeout.
     *
     * @param numSeconds The timeout value.
     */
    void setCommandTimeout(int numSeconds);

    /**
     * Returns the command execution timeout value.
     * Value equals to 0 means no timeout.
     *
     * @return The timeout value.
     */
    int getCommandTimeout();

    /**
     * The command timeout reset value.
     */
    enum TIMEOUT_RESET_VALUE {
        CONFIG,
        DEFAULT;
    }

    /**
     * Reset the timeout value.
     *
     * @param value The enumerated timeout reset value.
     */
    void resetTimeout(TIMEOUT_RESET_VALUE value);

    /**
     * Execute an operation. This call is guarded by the command timeout.
     * @param mn The operation.
     * @param description Operation description, used in exception message.
     * @return The response.
     * @throws CommandLineException If an exception occurs.
     * @throws java.io.IOException If an IOException occurs.
     */
    ModelNode execute(ModelNode mn, String description)
            throws CommandLineException, IOException;

    /**
     * Execute an operation. This call is guarded by the command timeout.
     * @param op The operation.
     * @param description Operation description, used in exception message.
     * @return The response.
     * @throws CommandLineException If an exception occurs.
     * @throws java.io.IOException If an IOException occurs.
     */
    ModelNode execute(Operation op, String description)
            throws CommandLineException, IOException;
}
