'use strict';
const $$ = (selector, root=document) => Array.from(root.querySelectorAll(selector));
const $ = (selector, root=document) => root.querySelector(selector);
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
$('#year').textContent = String(new Date().getFullYear());

// The live phone is an illustrative, ephemeral demo — never saved to a server or device.
const waterTotal = $('#phone-total');
const demoStatus = $('#demo-status');
const tabs = $$('.phone-tab');
let total = 1250;
let demoScreen = 'home';
function showScreen(name){
  demoScreen = name;
  tabs.forEach(button=>{
    const active = button.dataset.tab===name;
    button.classList.toggle('active',active);
    button.setAttribute('aria-pressed',String(active));
  });
  $$('.mock-screen').forEach(screen=>{
    const active = screen.dataset.screen===name;
    screen.hidden=!active;
    screen.classList.toggle('is-current',active);
  });
  $('#mock-reminder').textContent = name==='reminders' ? '11:00 am' : '11:00 am';
  demoStatus.textContent = `${name} preview shown`;
  phoneWater?.splash(.4,.6,.65);
}
tabs.forEach(button=>button.addEventListener('click',()=>showScreen(button.dataset.tab)));
function addDemoDrink(amount){
  total = Math.min(total + amount,5000);
  waterTotal.textContent = new Intl.NumberFormat('en-US').format(total);
  phoneWater?.setFill(Math.min(.83,total/2400));
  phoneWater?.splash(.55,.55,1.8);
  demoStatus.textContent = `Added ${amount} milliliters to the preview. Illustrative total: ${total} milliliters.`;
}
$$('.mock-tile').forEach(button=>button.addEventListener('click',()=>addDemoDrink(Number(button.dataset.amount))));
$('#mock-add').addEventListener('click',()=>addDemoDrink(250));

/** Lightweight water surface rendered in Canvas. Keeps all expensive drawing off when hidden. */
class LivingWater{
  constructor(canvas,options={}){
    this.canvas=canvas;
    this.ctx=canvas.getContext('2d',{alpha:true});
    this.fill=options.fill??.55;
    this.target=this.fill;
    this.mode=options.mode??'phone';
    this.wave=[];
    this.last=0;
    this.width=1;this.height=1;this.ratio=1;
    this.visible=true;
    this.running=false;
    this.dragging=false;
    this.bind();
    this.resize();
    if('ResizeObserver' in window){this.resizeObserver=new ResizeObserver(()=>this.resize());this.resizeObserver.observe(canvas)}
    if('IntersectionObserver' in window){this.visibilityObserver=new IntersectionObserver(entries=>{this.visible=entries[0].isIntersecting;if(this.visible)this.start()},{threshold:0});this.visibilityObserver.observe(canvas)}
    this.start();
  }
  bind(){
    const region=this.canvas;
    const pointer=(event,power)=>{
      const rect=region.getBoundingClientRect();
      this.splash((event.clientX-rect.left)/Math.max(1,rect.width),(event.clientY-rect.top)/Math.max(1,rect.height),power);
    };
    if(this.mode==='lab'){
      region.addEventListener('pointerdown',event=>{this.dragging=true;pointer(event,1.5);if(region.setPointerCapture)region.setPointerCapture(event.pointerId)});
      region.addEventListener('pointermove',event=>{if(this.dragging)pointer(event,.7)});
      region.addEventListener('pointerup',()=>{this.dragging=false});
      region.addEventListener('pointercancel',()=>{this.dragging=false});
    }else{
      const screen=region.parentElement;
      screen.addEventListener('pointerdown',event=>{if(event.target.closest('button'))return;pointer(event,.7)},{passive:true});
    }
  }
  setFill(next){this.target=Math.max(.04,Math.min(.93,next));this.start()}
  splash(x,y,strength=1){
    if(prefersReducedMotion.matches)return;
    this.wave.push({x:Math.max(0,Math.min(1,x)),y:Math.max(0,Math.min(1,y)),power:strength,start:performance.now()});
    if(this.wave.length>20)this.wave.shift();
    this.start();
  }
  resize(){
    const rect=this.canvas.getBoundingClientRect();
    this.width=Math.max(1,rect.width);this.height=Math.max(1,rect.height);
    this.ratio=Math.min(window.devicePixelRatio||1,2);
    this.canvas.width=Math.max(1,Math.round(this.width*this.ratio));
    this.canvas.height=Math.max(1,Math.round(this.height*this.ratio));
    this.ctx.setTransform(this.ratio,0,0,this.ratio,0,0);
    this.draw(performance.now());
  }
  surface(x,time){
    const baseline=this.height*(1-this.fill);
    if(prefersReducedMotion.matches)return baseline;
    let offset=Math.sin(x*11-time*.00135)*Math.min(7,this.height*.012)+Math.sin(x*18+time*.00064)*Math.min(4,this.height*.006);
    for(const w of this.wave){
      const age=(time-w.start)/1000;
      if(age<0||age>2.9)continue;
      const dist=Math.abs(x-w.x);
      const envelope=Math.exp(-Math.pow(dist/(.07+age*.34),2));
      offset+=Math.sin((dist*6.8-age*2.9)*Math.PI*2)*envelope*Math.exp(-age*1.15)*w.power*this.height*.039;
    }
    return baseline+offset;
  }
  draw(now){
    const ctx=this.ctx,W=this.width,H=this.height;
    if(!ctx)return;
    ctx.clearRect(0,0,W,H);
    const time=prefersReducedMotion.matches?0:now;
    const surfacePath=new Path2D();
    for(let i=0;i<=94;i++){
      const x=i/94, y=this.surface(x,time);
      if(i===0)surfacePath.moveTo(0,y);else surfacePath.lineTo(x*W,y);
    }
    const fillPath=new Path2D(surfacePath);fillPath.lineTo(W,H);fillPath.lineTo(0,H);fillPath.closePath();
    const gradient=ctx.createLinearGradient(0,H*(1-this.fill),W,H);
    gradient.addColorStop(0,'#54e5d7');gradient.addColorStop(.42,'#29c6d0');gradient.addColorStop(1,'#157f9f');
    ctx.fillStyle=gradient;ctx.fill(fillPath);
    ctx.strokeStyle='rgba(213,255,244,.72)';ctx.lineWidth=this.mode==='phone'?1.5:2.3;ctx.stroke(surfacePath);
    const shine=ctx.createLinearGradient(0,H*(1-this.fill),0,H);
    shine.addColorStop(0,'rgba(219,255,247,.15)');shine.addColorStop(.15,'rgba(213,255,248,.025)');shine.addColorStop(1,'rgba(0,46,68,.20)');ctx.fillStyle=shine;ctx.fill(fillPath);
    if(!prefersReducedMotion.matches){
      for(const w of this.wave){
        const age=(now-w.start)/1000;
        if(age<0||age>1.5)continue;
        let radius=(18+age*(this.mode==='lab'?170:100));
        ctx.strokeStyle=`rgba(214,255,249,${Math.max(0,(1-age/1.5)*.31*w.power)})`;
        ctx.lineWidth=1.5;ctx.beginPath();ctx.arc(w.x*W,w.y*H,radius,0,Math.PI*2);ctx.stroke();
      }
    }
  }
  tick=(now)=>{
    if(!this.visible||document.hidden){this.running=false;return}
    this.fill+=(this.target-this.fill)*.055;
    this.wave=this.wave.filter(w=>now-w.start<3000);
    this.draw(now);
    if(prefersReducedMotion.matches){this.running=false;return}
    requestAnimationFrame(this.tick);
  }
  start(){if(this.running||!this.visible||document.hidden)return;this.running=true;requestAnimationFrame(this.tick)}
}
const phoneWater = new LivingWater($('#phone-water'),{fill:.55,mode:'phone'});
const labWater = new LivingWater($('#lab-water'),{fill:.49,mode:'lab'});
const labButton=$('#make-wave');
labButton.addEventListener('click',()=>{labWater.splash(.38,.55,2.8);labWater.splash(.7,.58,1.4);demoStatus.textContent='Water demonstration rippled.'});
document.addEventListener('visibilitychange',()=>{if(!document.hidden){phoneWater.start();labWater.start()}});
prefersReducedMotion.addEventListener?.('change',()=>{phoneWater.start();labWater.start()});
// Keep interactive demo honest: it demonstrates the feel, not real drink tracking.
