package net.wizartinteractive.dbmodels;

public enum CallType
{
	INCOMING(0),

	OUTGOING(1);

	int value;

	CallType(int _value)
	{
		value = _value;
	}

	public int getType()
	{
		return value;
	}

}
