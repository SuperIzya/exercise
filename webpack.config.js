const path = require('path');
const CircularDependencyPlugin = require('circular-dependency-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

const outputDir = path.resolve(__dirname, 'target', 'web');
const rules = [].concat(
  require('./web/build/js-rules'),
  require('./web/build/style-rules'),
  require('./web/build/module-style-rules'),
  require('./web/build/hbs-rules'),
);

const plugins = [
  new HtmlWebpackPlugin({
    inject: 'body',
    template: 'index.hbs',
    filename: 'index.html',
  }),
  new CircularDependencyPlugin({
    // exclude detection of files based on a RegExp
    exclude: /\.js$|\/node_modules\//,
    // add errors to webpack instead of warnings
    failOnError: true,
    // set the current working directory for displaying module paths
    cwd: process.cwd(),
  }),
  new CleanWebpackPlugin(outputDir)
];

module.exports = ({
  entry: path.resolve(__dirname, 'web', 'index.jsx'),
  output: {
    path: outputDir,
    publicPath: '/',
    filename: path.join('web', 'bundle-[hash].js')
  },
  context: path.resolve(__dirname, 'web'),
  mode: 'development',
  devtool: 'source-map',
  watchOptions: {
    ignored: ['*.scala', /node_modules/, /target/]
  },
  module: { rules },
  resolve: {
    extensions: ['*', '.jsx', '.js']
  },
  plugins
});
