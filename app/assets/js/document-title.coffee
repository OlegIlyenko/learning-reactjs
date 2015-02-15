# Shamelessly taken from here: https://github.com/gaearon/react-document-title
global.DocumentTitle = React.createClass
  displayName: 'DocumentTitle'

  propTypes: title: React.PropTypes.string

  statics:
    mountedInstances: []
    rewind: ->
      activeInstance = DocumentTitle.getActiveInstance()
      DocumentTitle.mountedInstances.splice 0
      if activeInstance
        return activeInstance.props.title
      return
    getActiveInstance: ->
      length = DocumentTitle.mountedInstances.length
      if length > 0
        return DocumentTitle.mountedInstances[length - 1]
      return
    updateDocumentTitle: ->
      if typeof document == 'undefined'
        return
      activeInstance = DocumentTitle.getActiveInstance()
      if activeInstance
        document.title = activeInstance.props.title
      return

  getDefaultProps: ->
    { title: '' }

  isActive: ->
    this == DocumentTitle.getActiveInstance()

  componentWillMount: ->
    DocumentTitle.mountedInstances.push this
    DocumentTitle.updateDocumentTitle()
    return

  componentDidUpdate: (prevProps) ->
    if @isActive() and prevProps.title != @props.title
      DocumentTitle.updateDocumentTitle()
    return

  componentWillUnmount: ->
    index = DocumentTitle.mountedInstances.indexOf(this)
    DocumentTitle.mountedInstances.splice index, 1
    DocumentTitle.updateDocumentTitle()
    return

  render: ->
    if @props.children
      React.Children.only @props.children
    else
      null