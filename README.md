# gh-npm-publish

This is a small clojure web app made to be deployed to heroku but can be deployed to any server with little modifications. It's purpose is to remove the two step process of pushing to Github and having to publish to NPM. It looks for version bumps and auto publishes once triggered via Github's or Travis-CI's webhooks.

I was inspired to write this software after being exposed to [github-heroku-jekyll-hook][1] by Lincoln Stoll (lstoll) and then dommmel.

## Prerequisites

- To run locally you will need [Leiningen][2] 1.7.0 or above installed.
- A [heroku][4] account to push (with some modification this can be deployed to other providers, perhaps [Joyent][5]).

## Running

To start a web server for the application, run:

    lein run -m gh-npm-publish.handler

## Usage

### Github

First, set a key on this app to "secure" deploys

    $ heroku config:add ACCESS_KEY=[secret]

Second, add the Github repo for this app 

    $ heroku config:add [appname]_GITHUB_REPO=git@githup.com/jamesakers/[appname].git

Third, add some NPM config info

    $ heroku config:add NPM_EMAIL=[your NPM email] NPM_USERNAME=[your NPM username]

Then, the important part. Add your local npm information to be used during publishing. Make sure you have added yoursself as a user `npm adduser` and are logged in `npm login`. This wil ensure that all the required authentication keys are in place to be able to publish. Perhaps ther will be an more elegant way to do this in the future.

    $ heroku config:add NPM_AUTHORIZATION="$(cat [/path/to/your/.npmrc])"

This will most likely be found in your home directory `~/.npmrc`

Then, set up a github webhook pointing to a URL like

    https://gh-npm-publish-app.herokuapp.com/publish?app=[appname]&key=[secret]

You can view the output from pushes via `heroku logs`

This application supports multiple apps - just add a config for each app you want to deploy.

### Travis CI

This can also work with Travis CI, if you want to deploy on successful build. Base setup is the same as above, except instead of adding a webhook to GitHub add it to your .travis.yml like

    notifications:
      webhooks:
        urls:
          - https://gh-npm-publish-app.herokuapp.com/publish?app=[appname]&key=[secret]
        on_success: always
        on_failure: never

## License
Copyright © 2013 James Akers [BSD License][3]


[1]: https://github.com/dommmel/github-heroku-jekyll-hook
[2]: https://github.com/technomancy/leiningen
[3]: http://opensource.org/licenses/bsd-license.php
[4]: https://www.heroku.com/
[5]: https://joyent.com/
