import Vue from 'vue'
import { mount } from '@vue/test-utils'
import Navbar from '@/components/Navbar.vue'
import LocationDashboard from '@/views/LocationStatistics.vue'

describe('LocationDashboard.vue', () => {
  var wrapper = mount(LocationDashboard)

  it('mounts the navbar', () => {
    expect(wrapper.html()).toContain('<!--Navbar-->')
  })
  it('mounts the sidebar', () => {

  })
})
