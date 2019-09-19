package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.json.JSONException;
import org.json.JSONObject;

public class Engine
{
	final static double SECONDS_PER_MIN = 60.0;

	private static Engine instance;

	private static StudentDAO dao;

	private Engine()
	{
		dao = new StudentDAO();
	}

	public static synchronized Engine getInstance()
	{
		if (instance == null)
			instance = new Engine();
		return instance;
	}

	public String doPrime(String min, String max)
	{
		BigInteger bigIntMin = new BigInteger(min);
		BigInteger bigIntMax = new BigInteger(max);
		BigInteger next = bigIntMin.nextProbablePrime();
		if (next.compareTo(bigIntMax) > 0)
		{
			return "-1";
		} else
		{
			return next.toString();
		}
	}

	public List<StudentBean> doSIS(String prefix, String minGpa, String sortBy)
			throws SQLException, NumberFormatException
	{
			if (!minGpa.isEmpty())
			{
				double minGpaD = Double.parseDouble(minGpa);
				return dao.retrieve(prefix, minGpaD, sortBy);
			}
			return dao.retrieve(prefix, sortBy);
	}

	private static JSONObject readJsonFromUrl(URL url) throws JSONException, IOException
	{
		InputStream is = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String jsonText = "";
		String s;
		while ((s = reader.readLine()) != null)
		{
			jsonText = jsonText + s;
		}
		JSONObject json = new JSONObject(jsonText);
		return json;
	}

}
