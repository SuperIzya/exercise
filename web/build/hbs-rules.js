module.exports = [{
  test: /\.hbs$/,
  use: [{
    loader: "html-loader",
    options: {
      attrs: ["link:href"],
      interpolate: true
    }
  }, {
    loader: 'handlebars-render-loader',
    options: {
      data: {
        port: 8080
      }
    }
  }]
}];
