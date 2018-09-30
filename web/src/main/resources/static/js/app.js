new Vue({
  el: '#app',

  methods: {
    onDrowerItem() {
      console.log("================");
      console.log(arguments);
      console.log("================");
    }
  },
  
  created: function () {
    setInterval(function () {
      this.$data.counters.data[3].value++;
      this.$data.counters.data[4].value++;
      console.log(this.$data.counters.autoUpdate);
    }.bind(this), 1000); 
  },
  
  data: () => ({
    drawer: null,
    drawer_items: [
      { icon: 'track_changes', text: 'Counters' },
      { icon: 'import_export', text: 'Connections' },
      { divider: true },
      { icon: 'error_outline', text: 'Errors' },
    ],
    
    pagination: {
      sortBy: 'name'
    },
    
    counters: {
      autoUpdateTimeout: 5,
      autoUpdateList: Array.from(Array(11).keys()),
      
      headers: [
        { text: 'Counter', value: 'id',    align: 'right', sortable: true, width: 5 },
        { text: 'Name',    value: 'name',  align: 'left',  sortable: true },
        { text: 'Value',   value: 'value', align: 'right', sortable: false },
      ],

      data: [
        {
          id:  0,
          name: 'Bytes sent', 
          value: '292,870,080',
        },
        {
          id:  1,
          name: 'Bytes received', 
          value: '292,881,312',
        },
        {
          id:  2,
          name: 'Failed offers to ReceiverProxy', 
          value: 0,
        },
        {
          id:  3,
          name: 'Failed offers to SenderProxy', 
          value: 0,
        },
        {
          id:  4,
          name: 'Failed offers to DriverConductorProxy', 
          value: 0,
        },
        {
          id:  5,
          name: 'NAKs sent', 
          value: 0,
        },
        {
          id:  6,
          name: 'NAKs received', 
          value: 0,
        },
      ]
    }
  }),
})
