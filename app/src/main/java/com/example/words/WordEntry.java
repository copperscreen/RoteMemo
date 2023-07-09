package com.example.words;

import java.io.Serializable;

public class WordEntry implements Serializable {
	public String Key;
	public String Value;
	public boolean Answered;
	public WordEntry(String key, String value)
	{
		this.Key = key;
		this.Value = value;
		Answered = false;
	}
}
