<template>
    <lg-map-viz :access-token="accessToken" :layers="layers" :map-options="mapOptions"></lg-map-viz>
</template>

<script>
import 'bootstrap'
import LgMapViz from 'lg-map-viz'
import Config from '../../config/config'

export default {
  name: 'LocationTracker',
  data () {
    return {
      'accessToken': Config.mapboxApiKey,
      'mapOptions': {
        style: 'mapbox://styles/mapbox/streets-v10',
        center: [37, 0],
        zoom: 5
      },
      'config': Config,
      'searchParams': {
        'chvName': '',
        'chvId': '',
        'dates': ''
      },
      'layers': [{
        'id': 'route',
        'type': 'line',
        'source': {
          'type': 'geojson',
          'data': {
            'type': 'Feature',
            'properties': {},
            'geometry': {
              'type': 'LineString',
              'coordinates': [
                [37.48369693756104, 0.83381888486939],
                [37.48348236083984, 0.83317489144141],
                [37.48339653015138, 0.83270036637107],
                [37.48356819152832, 0.832056363179625],
                [37.48404026031496, 0.83114119107971],
                [37.48404026031496, 0.83049717427869],
                [37.48348236083984, 0.829920943955045],
                [37.48356819152832, 0.82954808664175],
                [37.48507022857666, 0.82944639795659],
                [37.48610019683838, 0.82880236636284],
                [37.48695850372314, 0.82931081282506],
                [37.48700141906738, 0.83080223556934],
                [37.48751640319824, 0.83168351665737],
                [37.48803138732912, 0.832158048267786],
                [37.48888969421387, 0.83297152392784],
                [37.48987674713133, 0.83263257682617],
                [37.49043464660643, 0.832937629287755],
                [37.49125003814696, 0.832429207817725],
                [37.49163627624512, 0.832564787218985],
                [37.49223709106445, 0.83337825839438],
                [37.49378204345702, 0.83368330777276]
              ]
            }
          }
        },
        'layout': {
          'line-join': 'round',
          'line-cap': 'round'
        },
        'paint': {
          'line-color': '#888',
          'line-width': 8
        }
      }]

    }
  },
  props: {},
  components: {
    LgMapViz
  },
  methods: {
    /**
             * @param {number} a - value.
             * @param {number} b - value.
             * @return {number} result.
             */
    selectChv: function (chv) {

    }
  },
  mounted: function () {

  }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style>
    @import "../assets/css/animate.min.css";
    @import "../assets/css/light-bootstrap-dashboard.css";
    @import "../assets/css/bootstrap.min.css";
    @import "../assets/css/pe-icon-7-stroke.css";

    #map {
        width: 100%;
        height: 90vh;
    }

    .el-range-separator{
        width: 10%!important;
    }

    #app {

        font-family: 'Avenir', Helvetica, Arial, sans-serif;

    }
    a, a:hover{
        text-decoration: none;
    }

    .sidebar {
        background-image: url("../assets/img/sidebar-5.jpg");
    }
</style>
