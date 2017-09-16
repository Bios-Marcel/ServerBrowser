package com.msc.serverbrowser.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class Command
{
	private static final List<Command>				commandsInternal	= new ArrayList<>();
	private static final ObservableList<Command>	commandsObservable	= FXCollections.observableList(commandsInternal);

	public static void registerCommand(final Command command)
	{
		commandsInternal.add(command);
	}

	private final List<Parameter>	parameters	= new ArrayList<>();
	private final String			name;

	public Command(final String name)
	{
		this.name = name;
	}

	abstract void execute(List<Parameter> params);

	public void addParameter(final Parameter parameter)
	{
		parameters.add(parameter);
	}

	public static class Parameter
	{
		private final String name;

		private Object value;

		private final Class<?> type;

		public <T extends Object> Parameter(final String name, final Class<T> type)
		{
			this.name = name;
			this.type = type;
		}
	}

}
