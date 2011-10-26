$(document).ready(function (){
	$(".addfriend").click(
			function() {
				
				$(this).html("Haciendo amigo...");
				$(this).load("/addfriend?friend=" + $(this)[0].id, null, function (response) {
							$(this).html(response);
							$(".indicadoramistad#" + $(this)[0].id)[0].src= "dedoarriba.png";
						});
			}
	);
	$(".addenemy").click(
			function() {
				$(this).html("Haciendo enemigo...")
				$(this).load("/addenemy?enemy=" + $(this)[0].id, null, function (response) {
							$(this).html(response);
							$(".indicadoramistad#" + $(this)[0].id)[0].src= "dedoenemigo.png";
						});
			}
	);
	$(".leavefriend").click(
			function() {
				$(this).html("Borrando amigo...")
				$(this).load("/leavefriend?friend=" + $(this)[0].id, null, function (response) {
							$(this).html(response);
							$(".indicadoramistad#" + $(this)[0].id)[0].src= "dedoabajo.png";
						});
			}
	);
	$(".leaveenemy").click(
			function() {
				$(this).html("Borrando enemigo...")
				$(this).load("/leaveenemy?enemy=" + $(this)[0].id, null, function (response) {
							$(this).html(response);
							$(".indicadoramistad#" + $(this)[0].id)[0].src= "dedoabajo.png";
						});
			}
	);
});
