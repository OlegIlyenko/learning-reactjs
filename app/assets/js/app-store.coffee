global.AppActions = Reflux.createActions [
  "addApp",
  "updateApp",
  "deleteApp"
]


global.AppListStore = Reflux.createStore
  listenables: [AppActions],

  apps: []
  maxId: 0
  localStorageKey: "apps"

  onAddApp: (app) ->
    @maxId += 1
    app.id = @maxId

    @apps.push app

    @update(@apps)

  onUpdateApp: (app) ->
    @apps[_.findIndex(@apps, (a) -> a.id == app.id)] = app

    @update(@apps)

  onDeleteApp: (id) ->
    @apps = _.remove(@apps, (a) -> a.id != id)

    @update(@apps)

  update: (list) ->
    localStorage.setItem @localStorageKey, JSON.stringify(list)
    @trigger(list)

  getInitialState: ->
    loadedList = localStorage.getItem(@localStorageKey)

    if !loadedList
      @apps = _.range(@maxId, @maxId + 100).map (i) ->
        id: i
        name: "App #{i}"
        status: if i % 2 == 0 then "ok" else "error"
    else
      @apps = JSON.parse(loadedList)

    @maxId = _.chain(@apps).map((app) -> app.id).max().value()

    @apps


global.UIActions = Reflux.createActions [
  "changeListState"
]

global.UIStore = Reflux.createStore
  listenables: [UIActions],

  listStyles: {
    apps: "list"
  }

  localStorageKey: "ui"

  state: ->
    listStyles: @listStyles

  onChangeListState: (cmp, style) ->
    @listStyles[cmp] = style
    @update()

  update: ->
    s = @state()

    localStorage.setItem @localStorageKey, JSON.stringify(s)
    @trigger s

  getInitialState: ->
    loadedList = localStorage.getItem(@localStorageKey)

    if loadedList?
      parsed = JSON.parse(loadedList)
      @listStyles = parsed.listStyles

    @state()