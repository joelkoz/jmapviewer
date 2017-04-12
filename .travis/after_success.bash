#!/usr/bin/env bash

git config --global credential.helper store
echo "https://${GH_USERNAME}:${GH_TOKEN}@github.com" > $HOME/.git-credentials

version=`git describe --always --dirty`

cd ../gh-pages
git stage --all -- maven
GIT_COMMITTER_NAME="Travis CI" GIT_COMMITTER_EMAIL="deploy@travis" GIT_AUTHOR_NAME="Travis CI" GIT_AUTHOR_EMAIL="deploy@travis" git commit -m "[$version] Update Maven repo"
git push origin gh-pages
