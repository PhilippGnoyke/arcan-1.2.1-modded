package it.unimib.disco.essere.analysis.file.visitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by IntelliJ IDEA.
 * User: bbejeck
 * Date: 1/23/12
 * Time: 10:29 PM
 */
public class CopyDirVisitor extends SimpleFileVisitor<Path> {

    private Path fromPath;
    private Path toPath;
    private StandardCopyOption copyOption;


    public CopyDirVisitor(Path fromPath, Path toPath, StandardCopyOption copyOption) {
        this.fromPath = fromPath;
        this.toPath = toPath;
        this.copyOption = copyOption;
    }

    public CopyDirVisitor(Path fromPath, Path toPath) {
        this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

        Path targetPath = toPath.resolve(fromPath.relativize(dir));
        if(!Files.exists(targetPath)){
//            Files.createDirectory(targetPath);//FIXME il java NIO è troppo anche per windows
        	 com.google.common.io.Files.createParentDirs(targetPath.toFile());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//    	Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);//FIXME il java NIO è troppo anche per windows
    	com.google.common.io.Files.createParentDirs(toPath.resolve(fromPath.relativize(file)).toFile());
        com.google.common.io.Files.copy(file.toFile(), toPath.resolve(fromPath.relativize(file)).toFile());
        return FileVisitResult.CONTINUE;
    }
}
