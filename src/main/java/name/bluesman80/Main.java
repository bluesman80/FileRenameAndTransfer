package name.bluesman80;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Stream;

public class Main
{
	private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "png");
	// For future use:
	//private static final List<String> VIDEO_EXTENSIONS = Arrays.asList("mpg", "mp4", "mpeg");

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Usage: frt <source path> <target path>");
			return;
		}

		String sourcePath = args[0];
		String targetImagePath = args[1];

		if (!Files.exists(Paths.get(sourcePath)))
		{
			System.err.println("Source path does not exist: " + sourcePath);
		}

		Path targetPathForImages = createPathIfNotExists(targetImagePath);
		if (targetPathForImages == null)
		{
			return;
		}

		try (Stream<Path> paths = Files.walk(Paths.get(sourcePath)).sorted())
		{
			Map<String, Integer> fileNameCountMap = new HashMap<>();

			int fileCount = 1;

			for (Iterator<Path> pathIterator = paths.iterator(); pathIterator.hasNext(); )
			{
				Path file = pathIterator.next();

				if (file.toFile().isFile())
				{
					final String fileName = file.getFileName().toString();
					final String fileExtension = fileName.substring(fileName.indexOf('.') + 1, fileName.length()).toLowerCase();

					if (IMAGE_EXTENSIONS.contains(fileExtension))
					{
						System.out.println(String.format("Found (%s): %s", fileCount++, fileName));

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

							String newName =
								formattedFileCreationTime + "_" + String.format("%03d", nextCount) + "." + fileExtension;

							final String targetSubPath = formattedFileCreationTime.substring(0, 6);

							targetPathForImages = createPathIfNotExists(targetImagePath + "/" + targetSubPath);

							if (targetPathForImages != null)
							{
								final Path finalTargetPath = Paths.get(targetPathForImages + "/" + newName);

								if (Files.exists(finalTargetPath))
								{
									//@formatter:off
									newName = newName.substring(0, newName.indexOf('.'))
										+ "-"
										+ String.valueOf(System.currentTimeMillis()).substring(8, 13)
										+ ".jpg";
									//@formatter:on
								}

								Files.copy(file, targetPathForImages.resolve(newName));

								System.out.println(String.format("\t\tCopied file to: %s as %s", targetSubPath, newName));
							}
							else
							{
								System.err.println("Error creating target sub-directory: " + targetSubPath);
							}

						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	private static Path createPathIfNotExists(String path)
	{
		final Path targetPath = Paths.get(path);

		if (!Files.exists(targetPath))
		{
			try
			{
				Files.createDirectory(targetPath);

				System.out.println("\nTarget path is created: " + path);
				System.out.println("\n\n");
			}
			catch (final IOException e)
			{
				System.err.println("Error creating the target directory: " + path);
				e.printStackTrace();
				return null;
			}
		}
		return targetPath;
	}

	private static String getFormattedFileCreationTime(final FileTime fileTime)
	{
		final String fileTimeString = fileTime.toString();
		return fileTimeString.substring(0, 10).replace("-", "");
	}
}
