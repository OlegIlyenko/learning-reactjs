global.ErrorActions = Reflux.createActions [
  "serverError"
  "clear"
]

global.AppActions = Reflux.createActions [
  "addApp"
  "updateApp"
  "deleteApp"

  "updated"
  "list"
  "subscribe"
  "unsubscribe"
]

updateHandler = (p) ->
  p.then AppActions.updated
  .fail ErrorActions.serverError

AppActions.addApp.listen (app) ->
  updateHandler($.ajax {url: "/api/apps", type: "POST", data: JSON.stringify(app), contentType: "application/json"})

AppActions.updateApp.listen (app) ->
  updateHandler($.ajax {url: "/api/apps/#{app.id}", type: "PUT", data: JSON.stringify(app), contentType: "application/json"})

AppActions.deleteApp.listen (id) ->
  updateHandler($.ajax {url: "/api/apps/#{id}", type: "DELETE"})

global.AppListStore = Reflux.createStore
  listenables: [AppActions],

  subs: 0
  eventSource: null

  onUpdated: (app) ->
    # SSE should update

  onSubscribe: ->
    @subs++
    @updateSubscription()

  onUnsubscribe: ->
    @subs--
    @updateSubscription()

  updateSubscription: ->
    if @eventSource? and @subs == 0
      @eventSource.close()
      @eventSource = null
    else if not @eventSource? and @subs > 0
      @eventSource = new EventSource("/api/apps/updates")

      @eventSource.addEventListener "message", (msg) =>
        @update()

      @eventSource.addEventListener "error", (e) =>
        @eventSource.close()
        @eventSource = null

        ErrorActions.serverError(e)

        Promise.delay(1000).then =>
          @updateSubscription()

  onList: () ->
    @update()

  update: () ->
    $.get("/api/apps")
    .then (list) =>
      @trigger(list)
    .fail ErrorActions.serverError


  getInitialState: ->
    []

global.ErrorStore = Reflux.createStore
  listenables: [ErrorActions],

  onServerError: (error) ->
    @trigger
      error: error
      message: error.statusText

  onClear: ->
    @trigger
      error: null
      message: null

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