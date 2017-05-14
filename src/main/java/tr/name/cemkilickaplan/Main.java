package tr.name.cemkilickaplan;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

public class Main
{

	public static void main(String[] args)
	{
		try (Stream<Path> paths = Files
			.walk(Paths.get("/Users/cemo/Documents")))
		{
			paths.forEach(file ->
			{
				if (file.toFile().isFile() && file.getFileName().toString().endsWith("jpg"))
				{
					System.out.println(file);

					try
					{
						BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);


						System.out.println(getFileTimeFormatted(attributes.creationTime()));
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

	}
	static String getFileTimeFormatted(final FileTime fileTime)
	{
		final String fileTimeString = fileTime.toString();
		return fileTimeString.substring(0, fileTimeString.indexOf('T')).replace("-", "");
	}
}
