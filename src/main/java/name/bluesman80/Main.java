package name.bluesman80;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Main
{
	private static String _sourcePath;// = "/Users/cemo/Downloads/FileTransferTrials";
	private static String _targetPathForImages;// = "/Users/cemo/Downloads/FileTransferTrials";
	private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "png");
	// For future use
	//private static final List<String> VIDEO_EXTENSIONS = Arrays.asList("mpg", "mp4", "mpeg");

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Usage: ftr <source path> <target path>");
			return;
		}

		_sourcePath = args[0];
		_targetPathForImages = args[1];

		if (!Files.exists(Paths.get(_sourcePath)))
		{
			System.err.println("Source path does not exist: " + _sourcePath);
		}

		final Path targetPathForImages = createPathIfNotExists(_targetPathForImages);
		if (targetPathForImages == null)
		{
			return;
		}

		try (Stream<Path> paths = Files.walk(Paths.get(_sourcePath)).sorted())
		{
			Map<String, Integer> fileNameCountMap = new HashMap<>();

			AtomicInteger fileCount = new AtomicInteger(1);
			paths.forEach(file ->
			{
				if (file.toFile().isFile())
				{
					final String fileName = file.getFileName().toString();
					final String fileExtension = fileName.substring(fileName.indexOf('.') + 1, fileName.length()).toLowerCase();

					if (IMAGE_EXTENSIONS.contains(fileExtension))
					{
						System.out.println(String.format("Found (%s): %s", fileCount.getAndIncrement(), fileName));

						try
						{
							BasicFileAttributes fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);

							final String formattedFileCreationTime = getFormattedFileCreationTime(fileAttributes.creationTime());

							if (!fileNameCountMap.containsKey(formattedFileCreationTime))
							{
								fileNameCountMap.put(formattedFileCreationTime, 0);
							}

							final Integer currentCount = fileNameCountMap.get(formattedFileCreationTime);
							final int nextCount = currentCount + 1;

							fileNameCountMap.replace(formattedFileCreationTime, nextCount);

							final String newName =
								formattedFileCreationTime + "_" + String.format("%03d", nextCount) + "." + fileExtension;

							Files.move(file, targetPathForImages.resolve(newName));

							System.out.println(String.format("\t\tMoved file to: %s as %s", _targetPathForImages, newName));
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			});
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	private static Path createPathIfNotExists(String targetPath)
	{
		final Path targetPathForImages = Paths.get(targetPath);
		if (!Files.exists(targetPathForImages))
		{
			try
			{
				Files.createDirectory(targetPathForImages);

				System.out.println("\nTarget path is created: " + targetPath);
				System.out.println("\n\n");
			}
			catch (final IOException e)
			{
				System.err.println("Error creating the target directory: " + targetPath);
				e.printStackTrace();
				return null;
			}
		}
		return targetPathForImages;
	}

	private static String getFormattedFileCreationTime(final FileTime fileTime)
	{
		final String fileTimeString = fileTime.toString();
		return fileTimeString.substring(0, fileTimeString.indexOf('T')).replace("-", "");
	}
}
