package com.jiyasoft.project.lowcode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

public class JGITExamples {

    public static void main(String[] args)
            throws IOException, InvalidRemoteException, TransportException, GitAPIException {

        // Local directory on this machine where we will clone remote repo.
        File localRepoDir = new File("/Users/girishpandit/Development/test");

        // Monitor to get git command progress printed on java System.out console
        TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));

        /*
         * Git clone remote repo into local directory.
         *
         * Equivalent of --> $ git clone https://github.com/Ravikharatmal/test.git
         */
        System.out.println("\n>>> Cloning repository\n");
        Repository repo = Git.cloneRepository().setProgressMonitor(consoleProgressMonitor).setDirectory(localRepoDir)
                .setURI("https://github.com/Ravikharatmal/test.git").call().getRepository();

        try (Git git = new Git(repo)) {
            /*
             * Get list of all branches (including remote) & print
             *
             * Equivalent of --> $ git branch -a
             *
             */
            System.out.println("\n>>> Listing all branches\n");
            git.branchList().setListMode(ListMode.ALL).call().stream().forEach(r -> System.out.println(r.getName()));

            // Find develop branch from remote repo.
            Optional<String> developBranch = git.branchList().setListMode(ListMode.REMOTE).call().stream()
                    .map(r -> r.getName()).filter(n -> n.contains("develop")).findAny();

            /*
             * If develop branch present then checkout.
             *
             * Equivalent of --> $ git checkout -b local-develop /remotes/origin/develop
             */
            if (developBranch.isPresent()) {
                System.out.println("\n>>> Checking out develop branch\n");
                git.checkout().setProgressMonitor(consoleProgressMonitor).setCreateBranch(true).setName("local-develop")
                        .setStartPoint(developBranch.get()).call();
            }

            // Modify one file & append a line
            System.out.println("\n>>> Modifying fileInDevelop.txt\n");
            File fileInDevelop = Arrays.stream(localRepoDir.listFiles())
                    .filter(f -> f.getName().contains("fileInDevelop.txt")).findFirst().get();
            Files.asCharSink(fileInDevelop, Charset.defaultCharset(), FileWriteMode.APPEND).write("Added by Program");
            /*
             * Check status of modified file.
             *
             * Equivalent of --> $ git status
             *
             */
            System.out.println("\n>>> Printing status of local repository\n");
            Status status = git.status().setProgressMonitor(consoleProgressMonitor).call();
            System.out.println("Modified file = " + status.getModified());

            /*
             * Stage modified files and Commit changes .
             *
             * Equivalent of --> $ git commit -a
             *
             */
            System.out.println("\n>>> Committing changes\n");
            RevCommit revCommit = git.commit().setAll(true).setMessage("Adding commit from JGIT").call();
            System.out.println("Commit = " + revCommit.getFullMessage());

            /*
             * Verify commit log
             *
             * Equivalent of --> $ git log
             *
             */
            System.out.println("\n>>> Printing commit log\n");
            Iterable<RevCommit> commitLog = git.log().call();
            commitLog.forEach(r -> System.out.println(r.getFullMessage()));

        }

    }

}

