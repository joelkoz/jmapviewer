#!/usr/bin/env bash

currentDir=`pwd`

# FULL_CLONE_DIR is the directory where the local clone of the JOSM repository resides
if [ -z "$FULL_CLONE_DIR" ]; then
  echo "██ Please set the variable FULL_CLONE_DIR"
  exit 1
fi
FULL_CLONE_DIR=`readlink -f "$FULL_CLONE_DIR"`
echo "██ FULL_CLONE_DIR = $FULL_CLONE_DIR"
FULL_CLONE_DIR=`readlink -f "$FULL_CLONE_DIR"`

if [ ! -z "$GH_USERNAME" ] && [ ! -z "$GH_TOKEN" ]; then
  git config --global credential.helper store
  echo "https://${GH_USERNAME}:${GH_TOKEN}@github.com" > $HOME/.git-credentials
fi

# If the GitHub repo is not already cloned (it should be cached between builds), clone it now
if [ ! -f "$FULL_CLONE_DIR/.git/config" ]; then
  echo "██ Clone current state from GitHub…"
  git clone "https://github.com/$TRAVIS_REPO_SLUG.git" "$FULL_CLONE_DIR"
  echo "██ Configure SVN remote…"
  cd "$FULL_CLONE_DIR"
  git svn init --trunk="" --ignore-paths="releases" --prefix="svn/" "https://svn.openstreetmap.org/applications/viewer/jmapviewer"
  git svn init -R releases --tags="releases" --prefix="svn/" "https://svn.openstreetmap.org/applications/viewer/jmapviewer"
else
  echo "██ Use clone in directory $FULL_CLONE_DIR which is already there…"
  cd "$FULL_CLONE_DIR"
  git fetch origin
fi

echo "██ Fetch from SVN…"
git svn fetch --quiet
git svn fetch --quiet -R releases

# Push all SVN branches to the GitHub repository
svnBranches=`git branch -r --no-color --list "svn/*"`
readarray -t lines <<<"$svnBranches"
for svnBranch in "${lines[@]}"; do
  # Trim the string
  svnBranch=`echo "$svnBranch" | xargs echo`
  echo "██ Push current state of branch $svnBranch to GitHub…"
  # Push the branch to the GitHub repo
  git push origin "refs/remotes/$svnBranch:refs/heads/$svnBranch"
  if [ $? != 0 ]; then
    echo "██ Could not push to GitHub"
    exit 1
  fi
done
