package grails.ui.command

import grails.dev.commands.ApplicationCommand
import grails.dev.commands.ApplicationContextCommandRegistry
import grails.dev.commands.ExecutionContext
import groovy.transform.CompileStatic
import org.springframework.beans.factory.config.AutowireCapableBeanFactory

/**
 * Delegates execution to another command that may be only known to the grails runtime but not to the gradle plugin.
 *
 * @author Yves Fischer
 * @since 3.1
 */
@CompileStatic
class RunCommandCommand implements ApplicationCommand {
    @Override
    boolean handle(ExecutionContext executionContext) {
        final args = executionContext.commandLine.remainingArgs
        assert args.size() > 0: 'Need a command name and maybe arguments'

        final command = ApplicationContextCommandRegistry.findCommand(args.first())
        assert command: "Failed to find command with name ${args.first()}. Available commands are: ${availableCommands}"

        executionContext.commandLine.remainingArgs.clear()
        executionContext.commandLine.remainingArgs.addAll(args.drop(1))

        command.setApplicationContext(applicationContext)
        applicationContext.autowireCapableBeanFactory.autowireBeanProperties(
                command, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false)

        try {
            return command.handle(executionContext)
        } catch (Exception e) {
            e.printStackTrace()
            throw e
        }
    }

    private static String getAvailableCommands() {
        return ApplicationContextCommandRegistry.findCommands().collect { it.name }.join(', ')
    }
}
