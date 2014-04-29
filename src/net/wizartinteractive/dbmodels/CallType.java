package net.wizartinteractive.dbmodels;

public enum CallType
{
	INCOMING(1),

	OUTGOING(2);

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
