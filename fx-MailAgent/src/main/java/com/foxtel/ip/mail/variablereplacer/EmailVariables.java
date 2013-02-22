package com.foxtel.ip.mail.variablereplacer;

import java.util.HashMap;
import java.util.Map;

public class EmailVariables
{
	public Map<String, String> variables = new HashMap<String, String>();

	public EmailVariables()
	{

	}

	public void setVariable(String name, String value)
	{
		variables.put(name, value);
	}

	/**
	 * 
	 * @param template
	 *            a template, e.g. "We just broadcast [title]."
	 * @return
	 */
	public String format(final String template)
	{
		String content = template;

		for (String variableName : variables.keySet())
		{
			final String value = variables.get(variableName);

			content = content.replace("[" + variableName + "]", value);
		}

		return content;
	}
}
