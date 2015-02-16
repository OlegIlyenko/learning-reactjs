{Grid, Row, Col, Navbar, Nav, NavItem, DropdownButton, MenuItem, ButtonGroup, Button, Table, Panel, Glyphicon, Modal, ModalTrigger, Input, Alert} = ReactBootstrap
{HistoryLocation, DefaultRoute, Link, Route, Redirect, RouteHandler} = ReactRouter
{NavItemLink, ButtonLink} = ReactRouterBootstrap

ListToggle = React.createClass
  displayName: "ListToggle"
  propTypes:
    type: React.PropTypes.string.isRequired
    cmpName: React.PropTypes.string.isRequired,

  render: ->
    <ButtonGroup className={@props.className}>
      <Button active={@props.type is "list"} onClick={=> UIActions.changeListState @props.cmpName, "list"}>
        <Glyphicon glyph="th-list" />
      </Button>

      <Button active={@props.type is "grid"} onClick={=> UIActions.changeListState @props.cmpName, "grid"}>
        <Glyphicon glyph="th-large" />
      </Button>
    </ButtonGroup>

AppEdit = React.createClass
  displayName: "AppEdit"
  propTypes:
    app: React.PropTypes.object

  getInitialState: ->
    name: if @props.app? then @props.app.name else ''

  save: (e) ->
    if e? then e.preventDefault()

    if @props.app?
      AppActions.updateApp
        id: @props.app.id
        name: @state.name
    else
      AppActions.addApp
        name: @state.name

    @props.onRequestHide()

  render: ->
    <Modal title={if @props.app? then "Edit Application" else "Add Application"} onRequestHide={@props.onRequestHide}>
      <div className="modal-body" onSublit={@save}>
        <form className="form-horizontal" onSubmit={@save}>
          <Input type="text" label="Name" labelClassName="col-xs-2" wrapperClassName="col-xs-10" ref="nameInput"
            value={@state.name} onChange={(e) => @setState {name: e.target.value}} />
        </form>
      </div>
      <div className="modal-footer">
        <Button onClick={@save} bsStyle="primary">{if @props.app? then "Save" else "Add"}</Button>
        <Button onClick={@props.onRequestHide}>Close</Button>
      </div>
    </Modal>

  componentDidMount: ->
    $("input", @refs.nameInput.getDOMNode()).focus()

ConfirmDialog = React.createClass
  displayName: "AppEdit"
  propTypes:
    title: React.PropTypes.string.isRequired
    text: React.PropTypes.string.isRequired
    onOk: React.PropTypes.func.isRequired

  onOk: ->
    @props.onOk()
    @props.onRequestHide()

  render: ->
    <Modal title={@props.title} onRequestHide={@props.onRequestHide}>
      <div className="modal-body" onSublit={@save}>
        {@props.text}
      </div>
      <div className="modal-footer">
        <Button onClick={@onOk}>Ok</Button>
        <Button onClick={@props.onRequestHide} bsStyle="primary" ref="cancelButton">Cancel</Button>
      </div>
    </Modal>

  componentDidMount: ->
    @refs.cancelButton.getDOMNode().focus()

StatusWidget = React.createClass
  displayName: "StatusWidget"

  componentDidMount: ->
    if @props.colors.length > 1
      [head, tail...] = @props.colors
      @trans(tail.concat([head]))
    else
      d3
      .select @refs.innerCircle.getDOMNode()
      .transition()
      .style "fill", @props.colors[0]

  shouldComponentUpdate: (nextProps) ->
    @props.borderColor != nextProps.borderColor

  componentWillUpdate: ->
    @componentWillUnmount()

  componentDidUpdate: ->
    @componentDidMount()

  componentWillUnmount: ->
    d3
    .select @refs.innerCircle.getDOMNode()
    .interrupt()

  trans: (colors)->
    if @refs.innerCircle
      [head, tail...] = colors

      d3
      .select @refs.innerCircle.getDOMNode()
      .transition()
      .style "fill", head
      .duration 3000
      .each "end", =>
        @trans(tail.concat([head]))

  render: ->
    radius = @props.radius
    innerRadius = @props.radius * 0.65

    <svg className="status-indicator" width={radius * 2} height={radius * 2} className={@props.className}>
      <circle cx={radius} cy={radius} r={radius} fill={@props.borderColor} />
      <circle cx={radius} cy={radius} r={innerRadius} fill={@props.colors[0]} ref="innerCircle" />
    </svg>

Apps = React.createClass
  displayName: "Apps"
  mixins: [Reflux.connect(UIStore, "ui"), Reflux.connect(AppListStore, "apps")],

  componentDidMount: ->
    AppActions.list()
    AppActions.subscribe()

  componentWillUnmount: ->
    AppActions.unsubscribe()

  deleteApp: (id) ->
    AppActions.deleteApp id

  render: ->
    <Grid>
      <Row>
        <Col md={12}>
          <h3>Apps</h3>
        </Col>
      </Row>
      <Row>
        <Col md={12}>
          <ModalTrigger modal={<AppEdit />}>
            <Button><Glyphicon glyph="plus" /> Add App</Button>
          </ModalTrigger>
          <ListToggle type={@state.ui.listStyles.apps} cmpName="apps" className="pull-right" />
        </Col>
      </Row>

      {if @state.ui.listStyles.apps is "list" then @renderList() else @renderGrid()}
    </Grid>

  renderList: ->
    <Row>
      <Col md={12}>
        <Table responsive>
          <thead>
            <tr>
              <th></th>
              <th>ID</th>
              <th>Name</th>
              <th></th>
            </tr>
          </thead>
          <tbody>{
            _.map @state.apps, (app) =>
              <tr key={app.id}>
                <td width="30">{@renderStaus(app)}</td>
                <td width="50">{app.id}</td>
                <td>{app.name}</td>
                <td>{@renderButtons(app)}</td>
              </tr>
          }</tbody>
        </Table>
      </Col>
    </Row>

  renderButtons: (app) ->
    <div className="pull-right">
      <ModalTrigger modal={<AppEdit app={app}/>}>
        <a htrf="#" className="my-button">Edit</a>
      </ModalTrigger>
      &nbsp;&nbsp;
      <ModalTrigger modal={<ConfirmDialog title="Delete app #{app.id}" text="Do you really want to delete app '#{app.name}'" onOk={=> @deleteApp(app.id)} />}>
        <a href="#" className="my-button">Delete</a>
      </ModalTrigger>
    </div>

  renderStaus: (app) ->
    if app.status == "ok"
      <StatusWidget radius={10} borderColor="#E2F0DA" colors={["#ABD697"]} className="pull-right" />
    else
      <StatusWidget radius={10} borderColor="#FDE3E0" colors={["#F04B4C", "#F69B98"]} className="pull-right" />

  renderGrid: ->
    _(@state.apps).chunk(4).map (chunk, idx) =>
      <Row key={idx}>{
        _.map chunk, (app) =>
          name = _.trunc(app.name, 15)
          <Col md={3} key={app.id}>
            <Panel header={<div>{name} {@renderButtons(app)}</div>}>
              {name} {@renderStaus(app)}
            </Panel>
          </Col>
      }</Row>
    .value()

ErrorReporter = React.createClass
  displayName: "ErrorReporter"
  mixins: [Reflux.connect(ErrorStore)],

  render: ->
    <Grid>
      <Row>
        <Col md={12}>{
          if @state? and @state.error?
            <Alert bsStyle="danger" dismissAfter={5000} onDismiss={-> ErrorActions.clear()}>
              <strong>Oops!</strong> {@state.message}
            </Alert>
        }</Col>
      </Row>
    </Grid>

Group = React.createClass
    displayName: "Group"
    render: ->
      <div>{@props.children}</div>

Navigation = React.createClass
  displayName: "Navigation"
  render: ->
    <Navbar brand="Learning React">
      <Nav>
        <NavItemLink eventKey={1} to="apps">Applications</NavItemLink>
        <NavItemLink eventKey={2} to="another">Something else</NavItemLink>
      </Nav>
    </Navbar>

AppsView = React.createClass
  displayName: "AppView"
  render: ->
    <DocumentTitle title="Applications">
      <Group>
        <Navigation />
        <ErrorReporter />
        <Apps />
      </Group>
    </DocumentTitle>

AnotherView = React.createClass
  displayName: "AnotherView"
  render: ->
    <DocumentTitle title="Another thing">
      <Group>
        <Navigation />
        <Grid><Row><Col md={12}>
          <h1>Hello World!</h1>
        </Col></Row></Grid>
      </Group>
    </DocumentTitle>

routes =
  <Route name="root" path="/">
    <Route name="apps" path="/apps" handler={AppsView} />
    <Route name="another" path="/another" handler={AnotherView} />
    <Redirect to="apps" />
  </Route>

ReactRouter.run routes, HistoryLocation, (Handler) ->
  React.render <Handler/>, document.getElementById('content')