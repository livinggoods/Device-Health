<template>
  <div id="app">

    <LocationTracker />
  </div>
</template>

<script>
import LocationTracker from './components/LocationTracker.vue'

export default {
  name: 'app',
  components: {
    LocationTracker
  }
}
</script>

<style>
#app {

  font-family: 'Avenir', Helvetica, Arial, sans-serif;

}
  a, a:hover{
    text-decoration: none;
  }
</style>
